package backend.academy.scrapper.Store;

import backend.academy.scrapper.models.Link;
import backend.academy.scrapper.models.LinkUpdate;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class JDBCStorage implements Storage {
    Connection con;

    public JDBCStorage(String url, String username, String password) throws SQLException {
        con = DriverManager.getConnection(url, username, password);
    }

    @Override
    public void addUser(Long user) throws IllegalArgumentException {
        try {

            if (checkUserExists(user)) {
                throw new IllegalArgumentException("user exists");
            }
            PreparedStatement addS = con.prepareStatement("""
                INSERT INTO users (chatId)
                SELECT ?
                WHERE NOT EXISTS (
                SELECT 1 FROM users WHERE chatId = ?
                )
                """);
            addS.setLong(1, user);
            addS.setLong(2, user);
            addS.execute();
            addS.close();
        } catch (SQLException se) {
            throw new RuntimeException(se);
        }
    }

    @Override
    public List<Link> getUserLinks(Long user) throws NoSuchElementException {
        List<Link> result = new ArrayList<>();
        try {

            if (!checkUserExists(user)) {
                throw new NoSuchElementException("No such user");
            }

            PreparedStatement s = con.prepareStatement(
                """
                    SELECT l.url, tags, filters
                    FROM users AS u
                    JOIN userLinks ON u.id = userId
                    JOIN links AS l ON linkId = l.id
                    WHERE u.chatId = ?
                    """
            );
            s.setLong(1, user);
            ResultSet rs = s.executeQuery();

            while (rs.next()) {
                result.add(
                    new Link(
                        rs.getString(1),
                        Arrays.stream((String[]) rs.getArray(3).getArray())
                            .collect(Collectors.toList()),
                        Arrays.stream((String[]) rs.getArray(2).getArray())
                            .collect(Collectors.toList())
                    )
                );
            }
            rs.close();
            s.close();
        } catch (SQLException se) {
            throw new RuntimeException(se);
        }
        return result;
    }

    @Override
    public void addLink(long user, Link link) throws IllegalArgumentException {
        if (!checkUserExists(user)) {
            throw new IllegalArgumentException("no user with such id");
        }

        try {
            PreparedStatement pr = con.prepareStatement("""
                SELECT COUNT(id)
                FROM links
                WHERE url = ?
                """);
            pr.setString(1, link.url());
            ResultSet rs = pr.executeQuery();
            rs.next();
            if (rs.getInt(1) == 0) {
                PreparedStatement addS = con.prepareStatement("""
                    INSERT INTO links (url)
                    SELECT ?
                    WHERE NOT EXISTS (
                    SELECT 1 FROM links WHERE url = ?
                    )
                    """);
                addS.setString(1, link.url());
                addS.setString(2, link.url());

                addS.execute();

                addS.close();
            }

            rs.close();

            pr = con.prepareStatement("""
                SELECT id
                FROM links
                WHERE url = ?
                """);
            pr.setString(1, link.url());
            rs = pr.executeQuery();
            rs.next();
            long linkId = rs.getLong(1);

            pr = con.prepareStatement("""
                SELECT id
                FROM users
                WHERE chatId = ?
                """);
            pr.setLong(1, user);
            rs = pr.executeQuery();
            rs.next();
            long userId = rs.getLong(1);


            PreparedStatement userlinkCheck = con.prepareStatement("""
                SELECT COUNT(linkId)
                FROM userLinks
                Where linkId = ? AND userId = ?
                """);
            userlinkCheck.setLong(1, linkId);
            userlinkCheck.setLong(2, userId);

            ResultSet check = userlinkCheck.executeQuery();
            check.next();
            if (check.getInt(1) == 0) {

                PreparedStatement insert = con.prepareStatement("""

                    INSERT INTO userLinks (linkId, userId, tags, filters)
                    SELECT ?, ?, ?, ?
                    WHERE NOT EXISTS (
                    SELECT 1 FROM userLinks WHERE linkId = ? and userId = ?
                    )
                    """);
                insert.setLong(1, linkId);
                insert.setLong(2, userId);
                insert.setArray(3, con.createArrayOf("TEXT", link.tags().toArray()));
                insert.setArray(4, con.createArrayOf("TEXT", link.filters().toArray()));
                insert.setLong(5, linkId);
                insert.setLong(6, userId);

                insert.execute();
            } else {
                throw new IllegalArgumentException("link is already being tracked");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteUser(long user) throws IndexOutOfBoundsException {
        if (!checkUserExists(user)) {
            throw new IndexOutOfBoundsException("No such user");
        }

        try {
            PreparedStatement ps = con.prepareStatement("""
                DELETE FROM users
                WHERE chatId = ?
                """);
            ps.setLong(1, user);
            ps.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeLink(long botUser, String link) throws IllegalArgumentException {
        if (!checkUserExists(botUser)) {
            throw new IllegalArgumentException("No such user");
        }
        try {
            PreparedStatement checkPr = con.prepareStatement("""
                SELECT COUNT(id) FROM links
                WHERE url = ?
                """);
            checkPr.setString(1, link);
            ResultSet checkRs = checkPr.executeQuery();
            checkRs.next();
            if (checkRs.getInt(1) == 0) {
                throw new NoSuchElementException("No value present");
            }

            PreparedStatement ps = con.prepareStatement("""
                DELETE FROM userLinks
                WHERE (linkId, userId) IN (
                SELECT linkId, userId from userLinks
                JOIN links as l ON linkId = l.id
                JOIN users as u ON userId = u.id
                WHERE l.url = ? AND u.chatId = ?
                )
                """);
            ps.setString(1, link);
            ps.setLong(2, botUser);
            ps.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<Link, LocalDateTime> getTimeLinks() {
        try {
            PreparedStatement pr = con.prepareStatement("""
                SELECT l.url, updatedAt, tags, filters
                FROM links l
                JOIN userLinks ON l.id = linkId
                LEFT OUTER JOIN linkUpdates AS lu ON lu.linkId = l.id
                """);
            ResultSet rs = pr.executeQuery();
            Map<Link, LocalDateTime> res = new HashMap<>();
            while (rs.next()) {
                Link l = new Link(
                    rs.getString(1),
                    Arrays.stream((String[]) rs.getArray(3).getArray())
                        .collect(Collectors.toList()),
                    Arrays.stream((String[]) rs.getArray(4).getArray())
                        .collect(Collectors.toList())
                );
                if (rs.getTimestamp(2) != null) {
                    res.put(l, rs.getTimestamp(2).toLocalDateTime());
                } else {
                    res.put(l, null);
                }
            }
            return res;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Long> getLinkChats(Link link) {

        try {
            PreparedStatement pr = con.prepareStatement("""
                SELECT u.chatId
                FROM users AS u
                JOIN userLinks ON u.id = userId
                JOIN links AS l ON linkId = l.id
                WHERE l.url = ?
                """);
            pr.setString(1, link.url());
            ResultSet rs = pr.executeQuery();
            List<Long> res = new ArrayList<>();
            while (rs.next()) {
                res.add(rs.getLong(1));
            }
            return res;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void registerUpdate(LinkUpdate update) {
        try {
            PreparedStatement linkPr = con.prepareStatement("""
                    SELECT id
                    FROM links
                    WHERE url = ?
                """);
            linkPr.setString(1, update.url());

            ResultSet linkrs = linkPr.executeQuery();
            linkrs.next();
            int linkId = linkrs.getInt(1);

            PreparedStatement pr = con.prepareStatement("""
                INSERT INTO linkUpdates (linkId, topic, updatedAt, username, preview)
                SELECT ?, ?, ?, ?, ?
                WHERE NOT EXISTS (
                SELECT 1 FROM linkUpdates WHERE linkId = ? and topic = ? and updatedAt = ?
                )
                """);
            pr.setInt(1, linkId);
            pr.setString(2, update.topic());
            pr.setTimestamp(3, Timestamp.valueOf(update.updateTime()));
            pr.setString(4, update.username());
            pr.setString(5, update.preview());
            pr.setInt(6, linkId);
            pr.setString(7, update.topic());
            pr.setTimestamp(8, Timestamp.valueOf(update.updateTime()));
            pr.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<LinkUpdate> getUpdatesAfter(LocalDateTime prevUpdateTime) {
        try {
            PreparedStatement pr = con.prepareStatement("""
                SELECT l.url, topic, preview, username, updatedAt
                FROM linkUpdates
                JOIN links l ON l.id = linkid
                WHERE updatedAt > ?
                """);
            pr.setTimestamp(1, Timestamp.valueOf(prevUpdateTime));
            ResultSet rs = pr.executeQuery();
            List<LinkUpdate> updates = new ArrayList<>();
            while (rs.next()) {
                updates.add(new LinkUpdate(
                    rs.getString(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4),
                    rs.getTimestamp(5).toLocalDateTime()
                ));
            }
            return updates;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean checkUserExists(Long user) {
        try {
            PreparedStatement s = con.prepareStatement("SELECT COUNT(id) FROM users WHERE chatId = ?");
            s.setLong(1, user);
            ResultSet rs = s.executeQuery();
            rs.next();
            boolean result = rs.getInt(1) == 1;
            rs.close();
            return result;
        } catch (SQLException se) {
            throw new RuntimeException(se);
        }
    }
}
