package donation;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// Donate page: doGet shows the form, doPost saves the donation
@WebServlet("/donate")
public class DonateServlet extends HttpServlet {

    // Shows the donation form
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        showForm(response, "", "");
    }

    // Called when the donor clicks the Donate button
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String name   = request.getParameter("name");
        String email  = request.getParameter("email");
        String method = request.getParameter("method");
        String campaignText = request.getParameter("campaign");
        String amountText   = request.getParameter("amount");

        // ---- Validation ----
        if (name == null || name.trim().equals("") ||
            email == null || email.trim().equals("")) {
            showForm(response, "Please enter your name and email.", "error");
            return;
        }
        if (!email.contains("@") || !email.contains(".")) {
            showForm(response, "Please enter a valid email address.", "error");
            return;
        }

        int amount = 0;
        int campaignId = 0;
        try {
            amount = Integer.parseInt(amountText);
            campaignId = Integer.parseInt(campaignText);
        } catch (Exception e) {
            showForm(response, "Please enter a valid amount.", "error");
            return;
        }
        if (amount <= 0) {
            showForm(response, "Amount must be greater than zero.", "error");
            return;
        }

        // ---- Save the donation in the database ----
        try {
            Connection con = DBConnection.getConnection();

            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO donations (donor_name, donor_email, campaign_id, amount, method) " +
                "VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, name.trim());
            ps.setString(2, email.trim());
            ps.setInt(3, campaignId);
            ps.setInt(4, amount);
            ps.setString(5, method);
            ps.executeUpdate();

            // Get the id of the new row and use it to make the receipt number
            ResultSet keys = ps.getGeneratedKeys();
            keys.next();
            String receipt = "RCP" + (1000 + keys.getInt(1));
            con.close();

            showForm(response, "Thank you " + PageUtil.safe(name.trim()) + "! Donation of Rs. "
                     + amount + " received. Receipt No: " + receipt, "success");

        } catch (Exception e) {
            showForm(response, "Could not save donation: " + e.getMessage(), "error");
        }
    }

    // Prints the form page with an optional message
    private void showForm(HttpServletResponse response, String message, String type)
            throws IOException {

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        PageUtil.header(out, "Donate");

        out.println("<h2>Donate Now</h2>");
        out.println("<div class='form-box'>");
        out.println("<form method='post' action='donate'>");

        out.println("<label>Your Name</label>");
        out.println("<input type='text' name='name' placeholder='Enter your name'>");

        out.println("<label>Email</label>");
        out.println("<input type='text' name='email' placeholder='Enter your email'>");

        out.println("<label>Select Campaign</label>");
        out.println("<select name='campaign'>");
        try {
            Connection con = DBConnection.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT id, name FROM campaigns");
            while (rs.next()) {
                out.println("<option value='" + rs.getInt("id") + "'>"
                            + PageUtil.safe(rs.getString("name")) + "</option>");
            }
            con.close();
        } catch (Exception e) {
            out.println("<option>Error loading campaigns</option>");
        }
        out.println("</select>");

        out.println("<label>Amount (Rs.)</label>");
        out.println("<input type='number' name='amount' placeholder='Enter amount'>");

        out.println("<label>Payment Method</label>");
        out.println("<select name='method'>");
        out.println("<option>UPI</option>");
        out.println("<option>Credit / Debit Card</option>");
        out.println("<option>Net Banking</option>");
        out.println("</select>");

        out.println("<button class='main-btn' type='submit'>Donate</button>");
        out.println("</form>");
        out.println("<p class='" + type + "'>" + message + "</p>");
        out.println("</div>");

        PageUtil.footer(out);
    }
}
