package backend.academy.scrapper.services;

import backend.academy.scrapper.Store.Storage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final Storage storage;

    public UserService(@Autowired Storage storage) {
        this.storage = storage;
    }

    public void addUser(long id) {
        storage.addUser(id);
    }

    public void deleteUser(long id) {
        storage.deleteUser(id);
    }
}
