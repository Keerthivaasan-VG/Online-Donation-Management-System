package donation;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// Home page: shows total amount, total donations and all campaigns with progress bars
@WebServlet("/home")
public class HomeServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        PageUtil.header(out, "Home");

        try {
            Connection con = DBConnection.getConnection();
            Statement st = con.createStatement();

            // Total amount and number of donations
            ResultSet rs = st.executeQuery(
                "SELECT COUNT(*), COALESCE(SUM(amount), 0) FROM donations");
            rs.next();
            int totalDonations = rs.getInt(1);
            int totalAmount = rs.getInt(2);

            // Number of campaigns
            rs = st.executeQuery("SELECT COUNT(*) FROM campaigns");
            rs.next();
            int totalCampaigns = rs.getInt(1);

            out.println("<h2>Make a difference today</h2>");
            out.println("<div class='stats'>");
            out.println("<div class='box'><span>" + totalAmount + "</span><p>Total Raised (Rs.)</p></div>");
            out.println("<div class='box'><span>" + totalDonations + "</span><p>Total Donations</p></div>");
            out.println("<div class='box'><span>" + totalCampaigns + "</span><p>Active Campaigns</p></div>");
            out.println("</div>");

            out.println("<h3>Current Campaigns</h3>");

            // Every campaign with the amount raised so far
            rs = st.executeQuery(
                "SELECT c.name, c.description, c.target_amount, " +
                "COALESCE(SUM(d.amount), 0) AS raised " +
                "FROM campaigns c LEFT JOIN donations d ON c.id = d.campaign_id " +
                "GROUP BY c.id, c.name, c.description, c.target_amount");

            while (rs.next()) {
                int target = rs.getInt("target_amount");
                int raised = rs.getInt("raised");
                int percent = raised * 100 / target;
                if (percent > 100) {
                    percent = 100;
                }

                out.println("<div class='card'>");
                out.println("<h4>" + PageUtil.safe(rs.getString("name")) + "</h4>");
                out.println("<p>" + PageUtil.safe(rs.getString("description")) + "</p>");
                out.println("<div class='bar'><div class='fill' style='width:" + percent + "%'></div></div>");
                out.println("<small>Rs. " + raised + " raised of Rs. " + target
                            + " (" + percent + "%)</small>");
                out.println("</div>");
            }
            con.close();

        } catch (Exception e) {
            out.println("<p class='error'>Error: " + e.getMessage() + "</p>");
        }

        PageUtil.footer(out);
    }
}
