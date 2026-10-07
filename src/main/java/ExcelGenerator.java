import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileOutputStream;

public class ExcelGenerator {
    public static void main(String[] args) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Questions");

        String[] headers = {
            "Loại Câu Hỏi (1=1 Đ/A, 2=Đ/S, 3=Điền, 4=Tự luận, 5=Nhiều Đ/A)",
            "Nội Dung Câu Hỏi",
            "Đáp Án A",
            "Đáp Án B",
            "Đáp Án C",
            "Đáp Án D",
            "Đáp Án Đúng (Ví dụ: A, B, hoặc văn bản mẫu)"
        };

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        Object[][] data = {
            {1, "Thủ đô của Việt Nam là gì?", "Hà Nội", "Hồ Chí Minh", "Đà Nẵng", "Hải Phòng", "A"},
            {2, "Mặt trời mọc ở hướng Tây?", "Đúng", "Sai", "", "", "B"},
            {3, "Điền vào chỗ trống: Học, học nữa, học ...", "", "", "", "", "mãi"},
            {4, "Hãy phân tích nguyên nhân dẫn đến sự bùng nổ của trí tuệ nhân tạo (AI).", "", "", "", "", "Sinh viên cần nêu được sự phát triển của phần cứng (GPU), dữ liệu lớn (Big Data)."},
            {5, "Những ngôn ngữ nào là ngôn ngữ lập trình?", "Python", "HTML", "Java", "CSS", "A, C"}
        };

        int rowNum = 1;
        for (Object[] rowData : data) {
            Row row = sheet.createRow(rowNum++);
            for (int i = 0; i < rowData.length; i++) {
                if (rowData[i] instanceof Integer) {
                    row.createCell(i).setCellValue((Integer) rowData[i]);
                } else {
                    row.createCell(i).setCellValue((String) rowData[i]);
                }
            }
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        FileOutputStream out = new FileOutputStream("Sample_Questions.xlsx");
        workbook.write(out);
        out.close();
        workbook.close();
        System.out.println("Excel file created successfully.");
    }
}
