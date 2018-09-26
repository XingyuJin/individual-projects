import java.util.HashMap;
import java.util.Map;

/**
 * This class provides all code necessary to take a query box and produce
 * a query result. The getMapRaster method must return a Map containing all
 * seven of the required fields, otherwise the front end code will probably
 * not draw the output correctly.
 */
public class Rasterer {
    private double d0 = (MapServer.ROOT_LRLON - MapServer.ROOT_ULLON) / MapServer.TILE_SIZE;
    private double latDis;
    private double lonDis;


    public Rasterer() {
        // YOUR CODE HERE
    }

    /**
     * Takes a user query and finds the grid of images that best matches the query. These
     * images will be combined into one big image (rastered) by the front end. <br>
     *
     *     The grid of images must obey the following properties, where image in the
     *     grid is referred to as a "tile".
     *     <ul>
     *         <li>The tiles collected must cover the most longitudinal distance per pixel
     *         (LonDPP) possible, while still covering less than or equal to the amount of
     *         longitudinal distance per pixel in the query box for the user viewport size. </li>
     *         <li>Contains all tiles that intersect the query bounding box that fulfill the
     *         above condition.</li>
     *         <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     *     </ul>
     *
     * @param params Map of the HTTP GET request's query parameters - the query box and
     *               the user viewport width and height.
     *
     * @return A map of results for the front end as specified: <br>
     * "render_grid"   : String[][], the files to display. <br>
     * "raster_ul_lon" : Number, the bounding upper left longitude of the rastered image. <br>
     * "raster_ul_lat" : Number, the bounding upper left latitude of the rastered image. <br>
     * "raster_lr_lon" : Number, the bounding lower right longitude of the rastered image. <br>
     * "raster_lr_lat" : Number, the bounding lower right latitude of the rastered image. <br>
     * "depth"         : Number, the depth of the nodes of the rastered image <br>
     * "query_success" : Boolean, whether the query was able to successfully complete; don't
     *                    forget to set this to true on success! <br>
     */
    public Map<String, Object> getMapRaster(Map<String, Double> params) {
        Map<String, Object> results = new HashMap<>();

        if (invalidInput(params)) {
            results.put("query_success", false);
            return results;
        } else {
            results.put("query_success", true);
        }

        int minDepth = getDepth(params);

        results.put("depth", minDepth);

        latDis = getNthLatDis(minDepth);
        lonDis = getNthLonDis(minDepth);

        int leftLon = (int) Math.ceil((params.get("ullon") - MapServer.ROOT_ULLON) / lonDis);
        int rightLon = (int) Math.ceil((params.get("lrlon") - MapServer.ROOT_ULLON) / lonDis);

        int leftLat = (int) Math.ceil((params.get("ullat") - MapServer.ROOT_ULLAT) / latDis);
        int rightLat = (int) Math.ceil((params.get("lrlat") - MapServer.ROOT_ULLAT) / latDis);

        int width = rightLon - leftLon + 1;
        int height = rightLat - leftLat + 1;



        String[][] renderGrid = new String[height][width];
        int xPos = 0;
        int yPos = 0;
        double rasterULLon = 0;
        double rasterULLat = 0;
        double rasterLRLon = 0;
        double rasterLRLat = 0;


        for (int i = 0; i < Math.pow(2, minDepth); i++) {
            if (getULLon(i, minDepth) > params.get("ullon")) {
                xPos = i - 1;
                rasterULLon = getULLon(xPos, minDepth);
                rasterLRLon = getLRLon(xPos + width - 1, minDepth);
                break;
            }
        }

        for (int i = 0; i < Math.pow(2, minDepth); i++) {
            if (getULLat(i, minDepth) < params.get("ullat")) {
                yPos = i - 1;
                rasterULLat = getULLat(yPos, minDepth);
                rasterLRLat = getLRLat(yPos + height - 1, minDepth);
                break;
            }
        }

        results.put("raster_ul_lon", rasterULLon);
        results.put("raster_ul_lat", rasterULLat);
        results.put("raster_lr_lon", rasterLRLon);
        results.put("raster_lr_lat", rasterLRLat);

        int row = 0;
        int col = 0;

        for (int x = xPos; x < xPos + width; x++) {
            for (int y = yPos; y < yPos + height; y++) {
                renderGrid[col][row] = "d" + Integer.toString(minDepth) + "_x"
                        + Integer.toString(x) + "_y" + Integer.toString(y) + ".png";
                col += 1;
            }
            row += 1;
            col = 0;
        }

        results.put("render_grid", renderGrid);

        return results;
    }

    private int getDepth(Map<String, Double> params) {
        double lonDPP = getLonDPP(params.get("lrlon"), params.get("ullon"), params.get("w"));
        int minDepth = 0;

        for (int i = 0; i <= 7; i++) {
            double tmp = getNthLDDP(i);
            minDepth = i;
            if (tmp <= lonDPP) {
                break;
            }
        }

        return minDepth;
    }

    private double getLonDPP(double lrlon, double ullon, double w) {
        return (lrlon - ullon) / w;
    }

    private double getNthLDDP(int n) {
        return d0 / Math.pow(2, n);
    }

    private double getULLon(int x, int depth) {
        return MapServer.ROOT_ULLON + x * lonDis;
    }

    private double getLRLon(int x, int depth) {
        return MapServer.ROOT_ULLON + (x + 1) * lonDis;
    }

    private double getULLat(int y, int depth) {
        return MapServer.ROOT_ULLAT + y * latDis;
    }

    private double getLRLat(int y, int depth) {
        return MapServer.ROOT_ULLAT + (y + 1) * latDis;
    }

    private double getNthLatDis(int depth) {
        return (MapServer.ROOT_LRLAT - MapServer.ROOT_ULLAT) / Math.pow(2, depth);
    }

    private double getNthLonDis(int depth) {
        return (MapServer.ROOT_LRLON - MapServer.ROOT_ULLON) / Math.pow(2, depth);
    }

    private boolean invalidInput(Map<String, Double> params) {
        return params.get("ullon") < MapServer.ROOT_ULLON
                || params.get("lrlon") > MapServer.ROOT_LRLON
                || params.get("ullon") > params.get("lrlon");
    }
}
