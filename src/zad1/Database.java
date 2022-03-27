package zad1;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class Database extends JFrame {
    private Statement stmt;
    private TravelData travelData;
    private boolean flag = false;
    private Locale lang;

    private JPanel panel = new JPanel();
    private JPanel panelLoc = new JPanel();
    private JTextArea log = new JTextArea(20, 40);

    public Database(String url, TravelData travelData) {
        try {
            Connection connection = DriverManager.getConnection(url);
            stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        this.travelData = travelData;
    }

    public void create() {
        try {
            stmt.executeUpdate("DROP TABLE oferty");
            stmt.executeUpdate("CREATE TABLE oferty (id INT PRIMARY KEY, destination VARCHAR(30), whenFly DATE, whenBack DATE," +
                    " place VARCHAR(20), price VARCHAR(25), language VARCHAR(10)) ");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        List<String> list = travelData.getShow();
        int counter = 1;
        for (String ele : list) {
            String country = "";
            for (int j = 0; j < ele.length(); j++) {
                if (ele.charAt(j) == '1' || ele.charAt(j) == '2') {
                    country = ele.substring(0, j - 1);
                    break;
                }
            }
            String language = "";
            for (int j = ele.length() - 1; j > 0; j--) {
                if (ele.charAt(j) == ' ') {
                    language = ele.substring(j + 1);
                    break;
                }
            }
            String[] tabIn = ele.substring(country.length() + 1, ele.length() - language.length()).split(" ");
            int s = country.length() + 1;
            for (int i = 0; i < 3; i++) {
                s += tabIn[i].length() + 1;
            }
            tabIn[3] = ele.substring(s, ele.length() - language.length() - 1);
            try {
                stmt.executeUpdate("INSERT INTO oferty VALUES (" + counter + ", '" + country + "', '" + tabIn[0] + "', '"
                        + tabIn[1] + "', '" + tabIn[2] + "', '" + tabIn[3] + "', '" + language + "')");
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            counter++;
        }
    }

    public void showGui() {
        panel.add(new JLabel());

        JButton b = new JButton("PL");
        b.setActionCommand("pl_PL");
        b.addActionListener(locChanger);
        panel.add(b);
        b = new JButton("EN");
        b.setActionCommand("en_GB");
        b.addActionListener(locChanger);
        panel.add(b);

        panelLoc.add(new JLabel());
        JButton bLoc = new JButton("JP");
        bLoc.setActionCommand("ja_JP");
        bLoc.addActionListener(whereChanger);
        panelLoc.add(bLoc);
        bLoc = new JButton("IT");
        bLoc.setActionCommand("it_IT");
        bLoc.addActionListener(whereChanger);
        panelLoc.add(bLoc);
        bLoc = new JButton("USA");
        bLoc.setActionCommand("en_US");
        bLoc.addActionListener(whereChanger);
        panelLoc.add(bLoc);

        add(panel, "North");
        add(panelLoc, "South");
        add(log);


        Font f = new Font("Dialog", Font.PLAIN, 18);
        log.setFont(f);
        for (Component c : panel.getComponents()) c.setFont(f);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    ActionListener locChanger = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            String symloc = e.getActionCommand();
            String[] locArg = symloc.split("_");
            lang = new Locale(locArg[0], locArg[1]);
            localize(lang);
        }
    };

    private void localize(Locale loc) {
        log.setText("");
        String line = "";
        Locale.setDefault(loc);
        ResultSet rs = null;
        try {
            rs = stmt.executeQuery("SELECT * FROM oferty");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        while (true) {
            try {
                if (loc.getISO3Country().equals(rs.getString(7)))
                    line += rs.getString(2) + ", " + rs.getString(3) + ", "
                            + rs.getString(4) + ", " + rs.getString(5) + ", " + rs.getString(6) + "\n";
            } catch (SQLException throwables) {
            }


            try {
                if (!rs.next()) break;
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        flag = true;
        log.setText(line);
    }

    ActionListener whereChanger = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            String symloc = e.getActionCommand();
            String[] locArg = symloc.split("_");
            whereGoing(new Locale(locArg[0], locArg[1]));
        }
    };

    private void whereGoing(Locale locale) {
        if (!flag) return;
        String line = "";
        AtomicReference<Locale> destination = new AtomicReference<>(new Locale(locale.getDisplayCountry()));
        Map map = travelData.createMap(lang);
        map.forEach((key, value) -> {
            if (key.toString().equals(locale.getDisplayCountry())) {
                destination.set(travelData.createLoc(value.toString()));
            }
        });
        log.setText(destination.get().getDisplayCountry());
        ResultSet rs = null;
        try {
            rs = stmt.executeQuery("SELECT * FROM oferty WHERE destination='" + destination.get().getDisplayCountry() + "'");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        while (true) {
            try {
                line += rs.getString(2) + ", " + rs.getString(3) + ", "
                        + rs.getString(4) + ", " + rs.getString(5) + ", " + rs.getString(6) + "\n";
            } catch (SQLException throwables) {
            }
            try {
                if (!rs.next()) break;
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        log.setText(line);
    }


}
