package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ProfileStore - singleton that holds per-session profile data:
 * - profile photo path (null = show initials)
 * - activity history (login events, password changes, etc.)
 */
public class ProfileStore {

    public record HistoryEntry(String title, String time, String date) {
    }

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("hh:mm a");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM / dd / yyyy");

    private static final ProfileStore INSTANCE = new ProfileStore();

    public static ProfileStore getInstance() {
        return INSTANCE;
    }

    private String profileImagePath = null;
    private int profileCropX = -1;
    private int profileCropY = -1;
    private int profileCropSize = -1;
    private final List<HistoryEntry> history = new ArrayList<>();

    private ProfileStore() {
        recordEvent("Login Activity");
    }

    public String getProfileImagePath() {
        return profileImagePath;
    }

    public void setProfileImagePath(String path) {
        profileImagePath = path;
    }

    public boolean hasProfileCrop() {
        return profileCropSize > 0;
    }

    public int getProfileCropX() {
        return profileCropX;
    }

    public int getProfileCropY() {
        return profileCropY;
    }

    public int getProfileCropSize() {
        return profileCropSize;
    }

    public void setProfileCrop(int x, int y, int size) {
        profileCropX = x;
        profileCropY = y;
        profileCropSize = size;
    }

    public List<HistoryEntry> getHistory() {
        return Collections.unmodifiableList(history);
    }

    public void recordEvent(String title) {
        LocalDateTime now = LocalDateTime.now();
        history.add(0, new HistoryEntry(title, now.format(TIME_FMT), now.format(DATE_FMT)));
    }
}
