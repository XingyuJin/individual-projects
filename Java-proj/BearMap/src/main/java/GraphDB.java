import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;


/**
 * Graph for storing all of the intersection (vertex) and road (edge) information.
 * Uses your GraphBuildingHandler to convert the XML files into a graph. Your
 * code must include the vertices, adjacent, distance, closest, lat, and lon
 * methods. You'll also need to include instance variables and methods for
 * modifying the graph (e.g. addNode and addEdge).
 *
 * @author Alan Yao, Josh Hug
 */
public class GraphDB {
    /** Your instance variables for storing the graph. You should consider
     * creating helper classes, e.g. Node, Edge, etc. */

    /**
     * Example constructor shows how to create and start an XML parser.
     * You do not need to modify this constructor, but you're welcome to do so.
     * @param dbPath Path to the XML file to be parsed.
     */

    Map<Long, Node> nodes = new LinkedHashMap<>();
    Map<Long, Highway> ways = new LinkedHashMap<>();
    Map<String, ArrayList<Location>> locs = new LinkedHashMap<>();
    Trie<Location> locPrefix = new Trie<>();
    Map<String, Long> locName = new LinkedHashMap<>();

    public GraphDB(String dbPath) {
        try {
            File inputFile = new File(dbPath);
            FileInputStream inputStream = new FileInputStream(inputFile);
            // GZIPInputStream stream = new GZIPInputStream(inputStream);

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            GraphBuildingHandler gbh = new GraphBuildingHandler(this);
            saxParser.parse(inputStream, gbh);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        clean();
    }

    /**
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     * @param s Input string.
     * @return Cleaned string.
     */
    static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

    /**
     *  Remove nodes with no connections from the graph.
     *  While this does not guarantee that any two nodes in the remaining graph are connected,
     *  we can reasonably assume this since typically roads are connected.
     */
    private void clean() {
        ArrayList<Long> nodesToClean = new ArrayList<>();

        for (Node n : nodes.values()) {
            if (n.getAdj().size() == 0) {
                nodesToClean.add(n.getID());
            }
        }
        nodes.keySet().removeAll(nodesToClean);
    }

    /**
     * Returns an iterable of all vertex IDs in the graph.
     * @return An iterable of id's of all vertices in the graph.
     */
    Iterable<Long> vertices() {
        return nodes.keySet();
    }

    /**
     * Returns ids of all vertices adjacent to v.
     * @param v The id of the vertex we are looking adjacent to.
     * @return An iterable of the ids of the neighbors of v.
     */
    Iterable<Long> adjacent(long v) {
        return nodes.get(v).getAdj();
    }

    /**
     * Returns the great-circle distance between vertices v and w in miles.
     * Assumes the lon/lat methods are implemented properly.
     * <a href="https://www.movable-type.co.uk/scripts/latlong.html">Source</a>.
     * @param v The id of the first vertex.
     * @param w The id of the second vertex.
     * @return The great-circle distance between the two locations from the graph.
     */
    double distance(long v, long w) {
        return distance(lon(v), lat(v), lon(w), lat(w));
    }

    static double distance(double lonV, double latV, double lonW, double latW) {
        double phi1 = Math.toRadians(latV);
        double phi2 = Math.toRadians(latW);
        double dphi = Math.toRadians(latW - latV);
        double dlambda = Math.toRadians(lonW - lonV);

        double a = Math.sin(dphi / 2.0) * Math.sin(dphi / 2.0);
        a += Math.cos(phi1) * Math.cos(phi2) * Math.sin(dlambda / 2.0) * Math.sin(dlambda / 2.0);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 3963 * c;
    }

    /**
     * Returns the initial bearing (angle) between vertices v and w in degrees.
     * The initial bearing is the angle that, if followed in a straight line
     * along a great-circle arc from the starting point, would take you to the
     * end point.
     * Assumes the lon/lat methods are implemented properly.
     * <a href="https://www.movable-type.co.uk/scripts/latlong.html">Source</a>.
     * @param v The id of the first vertex.
     * @param w The id of the second vertex.
     * @return The initial bearing between the vertices.
     */
    double bearing(long v, long w) {
        return bearing(lon(v), lat(v), lon(w), lat(w));
    }

    static double bearing(double lonV, double latV, double lonW, double latW) {
        double phi1 = Math.toRadians(latV);
        double phi2 = Math.toRadians(latW);
        double lambda1 = Math.toRadians(lonV);
        double lambda2 = Math.toRadians(lonW);

        double y = Math.sin(lambda2 - lambda1) * Math.cos(phi2);
        double x = Math.cos(phi1) * Math.sin(phi2);
        x -= Math.sin(phi1) * Math.cos(phi2) * Math.cos(lambda2 - lambda1);
        return Math.toDegrees(Math.atan2(y, x));
    }

    /**
     * Returns the vertex closest to the given longitude and latitude.
     * @param lon The target longitude.
     * @param lat The target latitude.
     * @return The id of the node in the graph closest to the target.
     */
    long closest(double lon, double lat) {
        double minDis = Double.POSITIVE_INFINITY;
        long closestID = 0L;

        for (Node n : nodes.values()) {
            double tmpDis = distance(lon, lat, n.getLon(), n.getLat());
            if (tmpDis < minDis) {
                minDis = tmpDis;
                closestID = n.getID();
            }
        }
        return closestID;
    }

    /**
     * Gets the longitude of a vertex.
     * @param v The id of the vertex.
     * @return The longitude of the vertex.
     */
    double lon(long v) {
        return nodes.get(v).getLon();
    }

    /**
     * Gets the latitude of a vertex.
     * @param v The id of the vertex.
     * @return The latitude of the vertex.
     */
    double lat(long v) {
        return nodes.get(v).getLat();
    }

    static class Node {
        private long id;
        private double lon;
        private double lat;
        private String name;
        private Set<Long> adj = new HashSet<>();

        Node(String id, String lon, String lat) {
            this.id = Long.parseLong(id);
            this.lon = Double.parseDouble(lon);
            this.lat = Double.parseDouble(lat);
        }

        void connectTo(Node n) {
            this.adj.add(n.getID());
        }

        void setName(String name) {
            this.name = name;
        }

        long getID() {
            return id;
        }

        double getLon() {
            return lon;
        }

        double getLat() {
            return lat;
        }

        Set<Long> getAdj() {
            return adj;
        }

        Location intoLoc() {
            return new Location(id, lon, lat, name);
        }
    }

    static class Highway {
        private long id;
        private String name;
        private double maxSpeed;
        private boolean validWay = false;

        Highway(String id) {
            this.id = Long.parseLong(id);
        }

        void setName(String name) {
            this.name = name;
        }

        void setMaxSpeed(String speed) {
            int blankIndex = speed.indexOf(" ");
            maxSpeed = Double.parseDouble(speed.substring(0, blankIndex));
        }

        long getID() {
            return id;
        }
    }

    static class Location {
        private long id;
        private double lon;
        private double lat;
        private String name;

        Location(long id, double lon, double lat, String name) {
            this.id = id;
            this.lon = lon;
            this.lat = lat;
            this.name = name;
        }

        long getID() {
            return id;
        }

        String getName() {
            return name;
        }

        double getLon() {
            return lon;
        }

        double getLat() {
            return lat;
        }
    }

    void addNode(Node n) {
        this.nodes.put(n.getID(), n);
    }

    void addway(Highway way) {
        this.ways.put(way.getID(), way);
    }

    void addloc(Location loc) {
        String cleanName = cleanString(loc.getName());
        if (!(cleanName.length() == 0)) {
            this.locPrefix.put(cleanName, loc);
        }
        if (!locs.containsKey(loc.getName())) {
            locs.put(loc.getName(), new ArrayList<>());
        }
        locs.get(loc.getName()).add(loc);
    }

    public List<String> getLocationsByPrefix(String prefix) {
        LinkedList<String> locNames = new LinkedList<>();
        if (locPrefix.size() == 0) {
            return locNames;
        }
        for (String key : locPrefix.keysWithPrefix(prefix)) {
            GraphDB.Location l = locPrefix.get(key);
            locNames.add(l.getName());
        }
        return locNames;
    }

    /**
     * Collect all locations that match a cleaned <code>locationName</code>, and return
     * information about each node that matches.
     * @param locationName A full name of a location searched for.
     * @return A list of locations whose cleaned name matches the
     * cleaned <code>locationName</code>, and each location is a map of parameters for the Json
     * response as specified: <br>
     * "lat" : Number, The latitude of the node. <br>
     * "lon" : Number, The longitude of the node. <br>
     * "name" : String, The actual name of the node. <br>
     * "id" : Number, The id of the node. <br>
     */
    public List<Map<String, Object>> getLocations(String locationName) {
        LinkedList<Map<String, Object>> locations = new LinkedList<>();
        if (locs.size() == 0) {
            return locations;
        }
        for (Location l : locs.get(locationName)) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", l.getName());
            map.put("lon", l.getLon());
            map.put("id", l.getID());
            map.put("lat", l.getLat());
            locations.add(map);
        }
        return locations;
    }
}
