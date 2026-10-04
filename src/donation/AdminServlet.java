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
import jakarta.servlet.http.HttpSession;

// Admin page: login, add campaign, delete campaign, logout.
// The "action" value sent by each form tells the servlet what to do.
@WebServlet("/admin")
public class AdminServlet extends HttpServlet {

    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_PASS = "admin123";

    // Opening the page: show the login form or the admin panel
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        // "logout" link
        if ("logout".equals(request.getParameter("action"))) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();      // remove the login
            }
            response.sendRedirect("admin");
            return;
        }

        if (isLoggedIn(request)) {
            showPanel(response, request.getParameter("msg"));
        } else {
            showLogin(response, "");
        }
    }

    // Submitting a form: login, add or delete
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String action = request.getParameter("action");

        // ---- Login ----
        if ("login".equals(action)) {
            String user = request.getParameter("username");
            String pass = request.getParameter("password");

            if (ADMIN_USER.equals(user) && ADMIN_PASS.equals(pass)) {
                HttpSession session = request.getSession();   // create session
                session.setAttribute("admin", "yes");
                response.sendRedirect("admin");
            } else {
                showLogin(response, "Wrong username or password!");
            }
            return;
        }

        // For add / delete the admin must already be logged in
        if (!isLoggedIn(request)) {
            response.sendRedirect("admin");
            return;
        }

        String message = "";
        try {
            Connection con = DBConnection.getConnection();

            // ---- Add a new campaign ----
            if ("add".equals(action)) {
                String name = request.getParameter("name");
                String desc = request.getParameter("description");
                int target = Integer.parseInt(request.getParameter("target"));

                if (name == null || name.trim().equals("") || target <= 0) {
                    message = "Enter a campaign name and a valid target amount.";
                } else {
                    PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO campaigns (name, description, target_amount) VALUES (?, ?, ?)");
                    ps.setString(1, name.trim());
                    ps.setString(2, desc);
                    ps.setInt(3, target);
                    ps.executeUpdate();
                    message = "Campaign added successfully.";
                }
            }

            // ---- Delete a campaign ----
            if ("delete".equals(action)) {
                int id = Integer.parseInt(request.getParameter("id"));

                // A campaign that already has donations is not deleted
                PreparedStatement check = con.prepareStatement(
                    "SELECT COUNT(*) FROM donations WHERE campaign_id = ?");
                check.setInt(1, id);
                ResultSet rs = check.executeQuery();
                rs.next();

                if (rs.getInt(1) > 0) {
                    message = "Cannot delete: this campaign already has donations.";
                } else {
                    PreparedStatement ps = con.prepareStatement(
                        "DELETE FROM campaigns WHERE id = ?");
                    ps.setInt(1, id);
                    ps.executeUpdate();
                    message = "Campaign deleted.";
                }
            }
            con.close();

        } catch (Exception e) {
            message = "Error: " + e.getMessage();
        }

        showPanel(response, message);
    }

    // Checks the session to know whether the admin has logged in
    private boolean isLoggedIn(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute("admin") != null;
    }

    // Login form
    private void showLogin(HttpServletResponse response, String message) throws IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        PageUtil.header(out, "Admin Login");

        out.println("<h2>Admin Panel</h2>");
        out.println("<div class='form-box'>");
        out.println("<form method='post' action='admin'>");
        out.println("<input type='hidden' name='action' value='login'>");
        out.println("<label>Username</label>");
        out.println("<input type='text' name='username' placeholder='Enter username'>");
        out.println("<label>Password</label>");
        out.println("<input type='password' name='password' placeholder='Enter password'>");
        out.println("<button class='main-btn' type='submit'>Login</button>");
        out.println("</form>");
        out.println("<p class='error'>" + message + "</p>");
        out.println("</div>");

        PageUtil.footer(out);
    }

    // Admin panel: add form + campaign table
    private void showPanel(HttpServletResponse response, String message) throws IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        PageUtil.header(out, "Admin Panel");

        out.println("<h2>Admin Panel &nbsp; <a class='logout' href='admin?action=logout'>Logout</a></h2>");
        if (message != null && !message.equals("")) {
            out.println("<p class='success'>" + PageUtil.safe(message) + "</p>");
        }

        out.println("<div class='form-box'>");
        out.println("<h3>Add New Campaign</h3>");
        out.println("<form method='post' action='admin'>");
        out.println("<input type='hidden' name='action' value='add'>");
        out.println("<label>Campaign Name</label>");
        out.println("<input type='text' name='name' placeholder='Eg: Education for All'>");
        out.println("<label>Description</label>");
        out.println("<input type='text' name='description' placeholder='Short description'>");
        out.println("<label>Target Amount (Rs.)</label>");
        out.println("<input type='number' name='target' placeholder='Eg: 100000'>");
        out.println("<button class='main-btn' type='submit'>Add Campaign</button>");
        out.println("</form></div>");

        out.println("<h3>Manage Campaigns</h3>");
        out.println("<table>");
        out.println("<tr><th>Campaign</th><th>Target</th><th>Raised</th><th>Action</th></tr>");

        try {
            Connection con = DBConnection.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(
                "SELECT c.id, c.name, c.target_amount, COALESCE(SUM(d.amount), 0) AS raised " +
                "FROM campaigns c LEFT JOIN donations d ON c.id = d.campaign_id " +
                "GROUP BY c.id, c.name, c.target_amount");

            while (rs.next()) {
                out.println("<tr>");
                out.println("<td>" + PageUtil.safe(rs.getString("name")) + "</td>");
                out.println("<td>Rs. " + rs.getInt("target_amount") + "</td>");
                out.println("<td>Rs. " + rs.getInt("raised") + "</td>");
                out.println("<td><form method='post' action='admin'>"
                          + "<input type='hidden' name='action' value='delete'>"
                          + "<input type='hidden' name='id' value='" + rs.getInt("id") + "'>"
                          + "<button class='delete-btn' type='submit'>Delete</button>"
                          + "</form></td>");
                out.println("</tr>");
            }
            con.close();
        } catch (Exception e) {
            out.println("<tr><td colspan='4'>Error: " + e.getMessage() + "</td></tr>");
        }

        out.println("</table>");
        PageUtil.footer(out);
    }
}
