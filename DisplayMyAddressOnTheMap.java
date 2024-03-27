import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import org.json.JSONObject;

public class DisplayMyAddressOnTheMap {

    private static final String OUTPUT_FILE_PATH = "C:\\CST7284\\output\\LatLong.csv";
    private static final String API_BASE_URL = "https://maps.googleapis.com/maps/api/geocode/json?";
    private static final String API_KEY = "AIzaSyCoW6R_Ayjhe7t02UTf_OJv599tntDFdRg";

    public static void main(String[] args) {
        String inputFilePath = "C:\\CST7284\\output\\OutputAddresses.csv";
        String line;

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
             FileWriter writer = new FileWriter(OUTPUT_FILE_PATH)) {

            // Write the header to the output file
            writer.write("Latitude,Longitude,Name,Icon,IconScale,IconAltitude\n");

            // Read the input file line by line
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");

                // Extract the address fields from the input
                String firstName = fields[0];
                String lastName = fields[1];
                String spouseFirstName = fields[2];
                String spouseLastName = fields[3];
                String streetNumber = fields[4];
                String streetName = fields[5];
                String streetType = fields[6];
                String streetOrientation = fields[7];
                String city = fields[8];
                String province = fields[9];

                // Build the URL request
                String address = buildAddressString(streetNumber, streetName, streetType, streetOrientation, city, province);
                String encodedAddress = URLEncoder.encode(address, "UTF-8");
                String urlString = API_BASE_URL + "address=" + encodedAddress + "&key=" + API_KEY;

                // Make the API request and retrieve the latitude and longitude
                JSONObject response = makeAPIRequest(urlString);
                JSONObject location = response.getJSONArray("results")
                        .getJSONObject(0)
                        .getJSONObject("geometry")
                        .getJSONObject("location");
                BigDecimal latitude = location.getBigDecimal("lat");
                BigDecimal longitude = location.getBigDecimal("lng");

                // Write the record to the output file
                String record = latitude + "," + longitude + "," + getFullName(firstName, lastName, spouseFirstName, spouseLastName) + ",111,1,1\n";
                writer.write(record);
            }

            System.out.println("Successfully generated the LatLong.csv file.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String buildAddressString(String streetNumber, String streetName, String streetType,
                                             String streetOrientation, String city, String province) {
        StringBuilder address = new StringBuilder();
        address.append(streetNumber).append(" ");
        address.append(streetName).append(" ");
        address.append(streetType).append(" ");
        address.append(streetOrientation).append(", ");
        address.append(city).append(", ");
        address.append(province);
        return address.toString();
    }

    private static JSONObject makeAPIRequest(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }

        return new JSONObject(response.toString());
    }

    private static String getFullName(String firstName, String lastName, String spouseFirstName, String spouseLastName) {
        StringBuilder fullName = new StringBuilder();
        fullName.append(firstName).append(" ").append(lastName);

        if (!spouseFirstName.isEmpty() && !spouseLastName.isEmpty()) {
            fullName.append(" and ").append(spouseFirstName).append(" ").append(spouseLastName);
        }

        return fullName.toString();
    }
}