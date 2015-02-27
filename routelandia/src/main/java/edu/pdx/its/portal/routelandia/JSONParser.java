/*
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package edu.pdx.its.portal.routelandia;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by loc on 1/17/15.
 */
public class JSONParser {
    /**
     * Parse the json array which download from the web
     * @param jsonArray
     * @return the list of highways which contain the latlng
     * so the Mapsactivity can draw freeway line on the map
     */
    public List<Highway> parse(JSONArray jsonArray){
        List<Highway> highwayList =  new ArrayList<>();
        try{
            for (int i = 0; i <jsonArray.length() ; i++) {
                //Create JSON Object for each array index
                JSONObject jsonObject = (JSONObject) jsonArray.get(i);

                //Get highwayid from the object
                int highwayid = jsonObject.getInt("highwayid");

                //Get highway name in the object
                String highwayname = jsonObject.getString("highwayname");

                //add the highway to the list highway
                highwayList.add(i, new Highway(highwayname, highwayid));

                JSONObject fullGeoJson = (JSONObject) jsonObject.get("fullGeoJson");

                //Create json array from coordinates
                JSONArray coordinates = (JSONArray) fullGeoJson.get("coordinates");

                //for each json array coordinates, create latlng and
                //add it to the list latlng of its highway
                for (int j = 0; j <coordinates.length() ; j++) {
                    double latitude = Double.parseDouble(((JSONArray) coordinates.get(j)).get(1).toString());
                    double longtitude = Double.parseDouble(((JSONArray) coordinates.get(j)).get(0).toString());
                    highwayList.get(i).addLatLng(new LatLng(latitude, longtitude));
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return highwayList;
    }

    public List<Highway> parseListOfHighWay(JSONArray jsonArray){
        List<Highway> highwayList =  new ArrayList<>();
        
        try{
            for (int i = 0; i <jsonArray.length() ; i++) {
                //Create JSON Object for each array index
                JSONObject jsonObject = (JSONObject) jsonArray.get(i);

                //Get highwayid from the object
                int highwayid = jsonObject.getInt("highwayid");

                //Get highway name in the object
                String highwayname = jsonObject.getString("highwayname");

                //add the highway to the list highway
                highwayList.add(i, new Highway(highwayname, highwayid));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return highwayList;
    }

    public List<Station> parseStationList(JSONArray jsonArray){
        List<Station> stationList =  new ArrayList<>();
        try{
            for (int i = 0; i <jsonArray.length() ; i++) {
                //Create JSON Object for each array index
                JSONObject jsonObject = (JSONObject) jsonArray.get(i);

                //Get stationid from the object
                int stationid = jsonObject.getInt("stationid");

                //Get linked list position in the object
//                int linkedListPosition = jsonObject.getInt("linked_list_position");

                //add the highway to the list highway
                stationList.add(new Station(stationid));

                if (!jsonObject.isNull("geojson_raw")) {
                    JSONObject geojsonRaw = jsonObject.getJSONObject("geojson_raw");

                    //Create json array from coordinates
                    JSONArray coordinates = (JSONArray) geojsonRaw.get("coordinates");

                    //for each json array coordinates, create latlng and
                    //add it to the list latlng of its station
                    for (int j = 0; j < coordinates.length(); j++) {
                        double latitude = Double.parseDouble(((JSONArray) coordinates.get(j)).get(1).toString());
                        double longtitude = Double.parseDouble(((JSONArray) coordinates.get(j)).get(0).toString());
                        stationList.get(i).addLatLng(new LatLng(latitude, longtitude));
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return stationList;
    }

    List<TravelingInfo> parseTravelingInfo(JSONArray jsonArray){
        List<TravelingInfo> travelingInfoList = new ArrayList<>();

        if(jsonArray == null){
            return null;
        }

        else {
            try {
                for (int i = 0; i < jsonArray.length(); i++) {
                    //Create JSON Object for each array index
                    JSONObject jsonObject = (JSONObject) jsonArray.get(i);

                    //get hour, minute, speed, and travle time from each json obj
                    int hour = Integer.parseInt(jsonObject.getString("hour"));
                    int minute = Integer.parseInt(jsonObject.getString("minute"));
                    double speed = 0.0;

                    if (!jsonObject.isNull("speed")) {
                        speed = Double.parseDouble(jsonObject.getString("speed"));
                    }
                    double travelTime = 0.0;
                    if (!jsonObject.isNull("traveltime")) {
                        travelTime = Double.parseDouble(jsonObject.getString("traveltime"));
                    }
                    //add new obj traveling info to the list
                    travelingInfoList.add(new TravelingInfo(hour, minute, speed, travelTime));

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return travelingInfoList;
        }
    }
}
