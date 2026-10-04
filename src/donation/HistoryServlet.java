package donation;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// History page: shows all donations, with a search by donor name
@WebServlet("/history")
public class HistoryServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        PageUtil.header(out, "Donation History");

        // Read the search word (it is empty when the page is opened normally)
        String search = request.getParameter("search");
        if (search == null) {
            search = "";
        }

        out.println("<h2>Donation History</h2>");
        out.println("<form method='get' action='history'>");
        out.println("<input type='text' name='search' id='searchBox' placeholder='Search by donor name...' value='"
                    + PageUtil.safe(search) + "'>");
        out.println("<button class='main-btn small' type='submit'>Search</button>");
        out.println("</form>");

        out.println("<table>");
        out.println("<tr><th>Receipt No</th><th>Donor</th><th>Campaign</th>"
                    + "<th>Amount</th><th>Method</th><th>Date</th></tr>");

        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(
                "SELECT d.id, d.donor_name, c.name, d.amount, d.method, d.donated_on " +
                "FROM donations d JOIN campaigns c ON d.campaign_id = c.id " +
                "WHERE d.donor_name LIKE ? ORDER BY d.id DESC");
            ps.setString(1, "%" + search + "%");
            ResultSet rs = ps.executeQuery();

            boolean found = false;
            while (rs.next()) {
                found = true;
                out.println("<tr>");
                out.println("<td>RCP" + (1000 + rs.getInt("id")) + "</td>");
                out.println("<td>" + PageUtil.safe(rs.getString("donor_name")) + "</td>");
                out.println("<td>" + PageUtil.safe(rs.getString("name")) + "</td>");
                out.println("<td>Rs. " + rs.getInt("amount") + "</td>");
                out.println("<td>" + PageUtil.safe(rs.getString("method")) + "</td>");
                out.println("<td>" + rs.getString("donated_on").substring(0, 10) + "</td>");
                out.println("</tr>");
            }
            if (!found) {
                out.println("<tr><td colspan='6'>No donations found.</td></tr>");
            }
            con.close();

        } catch (Exception e) {
            out.println("<tr><td colspan='6'>Error: " + e.getMessage() + "</td></tr>");
        }

        out.println("</table>");
        PageUtil.footer(out);
    }
}
