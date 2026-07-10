package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * AuditLog — Figure 2: system saves information in the audit logs
 * and updates the status as CLAIMED when a claim is confirmed.
 */
public class AuditLog {

    public static class Entry {
        private final String timestamp;
        private final String action;
        private final String itemName;
        private final String claimantName;
        private final String adminUser;

        public Entry(String action, String itemName, String claimantName, String adminUser) {
            this.timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            this.action = action;
            this.itemName = itemName;
            this.claimantName = claimantName;
            this.adminUser = adminUser;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public String getAction() {
            return action;
        }

        public String getItemName() {
            return itemName;
        }

        public String getClaimantName() {
            return claimantName;
        }

        public String getAdminUser() {
            return adminUser;
        }

        @Override
        public String toString() {
            return String.format("[%s] %s — Item: %s | Claimant: %s | Admin: %s",
                    timestamp, action, itemName, claimantName, adminUser);
        }
    }

    private static final List<Entry> LOG = new ArrayList<>();

    /** Figure 2: log a claim confirmation — status updated to CLAIMED. */
    public static void logClaim(String itemName, String claimantName, String adminUser) {
        LOG.add(new Entry("ITEM CLAIMED - status updated to CLAIMED", itemName, claimantName, adminUser));
        System.out.println("AUDIT: " + LOG.get(LOG.size() - 1));
    }

    /** Figure 1: log a new item posted to the dashboard. */
    public static void logNewPost(String itemName, String adminUser) {
        LOG.add(new Entry("NEW ITEM POSTED", itemName, "", adminUser));
        System.out.println("AUDIT: " + LOG.get(LOG.size() - 1));
    }

    public static List<Entry> getLog() {
        return Collections.unmodifiableList(LOG);
    }
}
