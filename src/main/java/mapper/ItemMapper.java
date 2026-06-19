package mapper;

import com.campuslf.models.ItemReport;
import model.Item;

public class ItemMapper {
    public static Item toItem(ItemReport report) {

        Item item = new Item();

        item.setName(report.getItemName());
        item.setLocation(report.getLocationFound());

        item.setDate(
                report.getDatePosted() != null
                        ? report.getDatePosted().toString()
                        : ""
        );

        item.setStudentId(report.getFinderStudentId());
        item.setContactNumber(report.getFinderContactNum());
        item.setImagePath(report.getImageUrl());

        // Temporarily store description as color
        item.setColor(report.getDescription());

        // Temporary category display
        item.setCategory("Category #" + report.getCategoryId());

        item.setReporterName("Admin");

        if ("Claimed".equalsIgnoreCase(report.getReportStatus())) {
            item.setStatus(Item.Status.FOUND);
        } else {
            item.setStatus(Item.Status.LOST);
        }

        return item;
    }
}
