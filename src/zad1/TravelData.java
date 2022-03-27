package zad1;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class TravelData {
    private List<String> show = new LinkedList<>();
    private List<String> data = new LinkedList<>();

    public TravelData(File dataDir) {
        Path path = Paths.get(dataDir.getAbsolutePath());
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                public FileVisitResult visitFile(Path file, BasicFileAttributes a) {
                    try {
                        Scanner scanner = new Scanner(file);
                        while (scanner.hasNextLine())
                            data.add(scanner.nextLine());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getOffersDescriptionsList(String locale, String dateFormat) {
        SimpleDateFormat df = (SimpleDateFormat) DateFormat.getDateInstance();
        df.applyPattern(dateFormat);
        List<String> resoult = new LinkedList<>();
        Map mapPl = createMap(new Locale("pl", "PL"));
        Map mapEn = createMap(new Locale("en", "GB"));
        Locale loc = createLoc(locale);
        for (String ele : data) {
            String linia = "";
            String[] table = ele.split("\t");
            Locale kontrachent = createLoc(table[0]);
            String dest = table[1];
            AtomicReference<Locale> destination = new AtomicReference<>(new Locale(dest));
            if (kontrachent.getDisplayName(new Locale("pl")).startsWith("polski")) {
                mapPl.forEach((key, value) -> {
                    if (key.toString().equals(dest)) {
                        destination.set(createLoc(value.toString()));
                    }
                });
            } else {
                mapEn.forEach((key, value) -> {
                    if (key.toString().equals(dest)) {
                        destination.set(createLoc(value.toString()));
                    }
                });
            }
            linia += destination.get().getDisplayCountry(loc) + " ";
            Date from = null, to = null;
            try {
                from = new SimpleDateFormat(dateFormat).parse(table[2]);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            try {
                to = new SimpleDateFormat(dateFormat).parse(table[3]);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            linia += df.format(from) + " " + df.format(to) + " ";
            linia += info(table[4], kontrachent, loc) + " ";
            NumberFormat numberFormatLoc = NumberFormat.getNumberInstance(loc);
            char[] number = table[5].toCharArray();
            if (kontrachent.getDisplayName(new Locale("pl")).startsWith("polski")) {
                String num = "";
                for (int i = 0; i < number.length; i++) {
                    if (number[i] == ',') {
                        number[i] = '.';
                    }
                    num += number[i];
                }
                linia += numberFormatLoc.format(Double.valueOf(num)) + " ";
            } else {
                String num = "";
                for (int i = 0; i < number.length; i++) {
                    if (number[i] == ',') continue;
                    num += number[i];
                }
                linia += numberFormatLoc.format(Double.valueOf(num)) + " ";
            }
            linia += table[6];
            resoult.add(linia);
        }
        for (String ele : resoult) {
            ele += " " + loc.getISO3Country();
            show.add(ele);
        }
        return resoult;
    }


    private String info(String line, Locale locFrom, Locale locTo) {
        String key = "";
        ResourceBundle msgs = ResourceBundle.getBundle("zad1.dictionary", locFrom);
        for (String ele : msgs.keySet()) {
            if (line.equals(msgs.getObject(ele))) {
                key = ele;
            }
        }
        ResourceBundle msgsTo = ResourceBundle.getBundle("zad1.dictionary", locTo);
        return msgsTo.getString(key);
    }

    public Locale createLoc(String loc) {
        String[] LocTab = loc.split("_");
        Locale locale;
        if (LocTab.length == 1) {
            locale = new Locale(LocTab[0]);
        } else if (LocTab.length == 2) {
            locale = new Locale(LocTab[0], LocTab[1]);
        } else locale = new Locale(LocTab[0], LocTab[1], LocTab[2]);
        return locale;
    }

    public Map createMap(Locale locale) {
        Locale[] location = Locale.getAvailableLocales();
        Map map = new HashMap();
        String country;
        for (int i = 0; i < location.length; i++) {
            String countryCode = location[i].getCountry();
            if (countryCode.equals("")) continue;
            country = location[i].getDisplayCountry(locale);
            map.put(country, location[i]);
        }
        return map;
    }

    public List<String> getShow() {
        return show;
    }
}
