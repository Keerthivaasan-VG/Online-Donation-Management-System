package donation;

import java.io.PrintWriter;

// Helper methods used by every servlet to print the common top and bottom of the page.
public class PageUtil {

    // Prints the top part of the page (header and menu)
    public static void header(PrintWriter out, String title) {
        out.println("<!DOCTYPE html>");
        out.println("<html><head><meta charset='UTF-8'>");
        out.println("<title>" + title + "</title>");
        out.println("<link rel='stylesheet' href='css/style.css'>");
        out.println("</head><body>");
        out.println("<header>");
        out.println("<h1>&#10084; Online Donation Management System</h1>");
        out.println("<nav>");
        out.println("<a href='home'>Home</a>");
        out.println("<a href='donate'>Donate</a>");
        out.println("<a href='history'>Donation History</a>");
        out.println("<a href='admin'>Admin</a>");
        out.println("</nav></header>");
        out.println("<div class='page'>");
    }

    // Prints the bottom part of the page
    public static void footer(PrintWriter out) {
        out.println("</div>");
        out.println("<footer>Internet Programming Lab Mini Project</footer>");
        out.println("</body></html>");
    }

    // Converts special characters so that user input cannot break the page
    public static String safe(String text) {
        if (text == null) {
            return "";
        }
        text = text.replace("&", "&amp;");
        text = text.replace("<", "&lt;");
        text = text.replace(">", "&gt;");
        text = text.replace("\"", "&quot;");
        text = text.replace("'", "&#39;");
        return text;
    }
}
