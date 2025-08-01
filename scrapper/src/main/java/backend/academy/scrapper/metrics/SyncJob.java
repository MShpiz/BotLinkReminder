//package backend.academy.scrapper.metrics;
//
//import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
//import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
//import org.springframework.boot.actuate.endpoint.annotation.Selector;
//import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//import java.util.Map;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.atomic.AtomicBoolean;
//
//@Component
//@Endpoint(id = "scrapperSync")
//public class SyncJob {
//    private final AtomicBoolean isEnabled= new AtomicBoolean(true);
//
//    @ReadOperation
//    public Map<String, Object> isEnabled() {
//        return Map.of(
//            "isEnabled", isEnabled.get(),
//            "description", ""
//        );
//    }
//
//    @WriteOperation
//    public String setEnabled(@Selector String isEnabled) {
//        var toEnable = Boolean.parseBoolean(isEnabled);
//        var wasEnabled = this.isEnabled.getAndSet(toEnable);
//        if (toEnable) {
//            return "enabled";
//        } else {
//            return "disabled";
//        }
//    }
//
//    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.SECONDS)
//    public void sync() {
//        if (!isEnabled.get()) {
//            return;
//        }
//
//    }
//}
