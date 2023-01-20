package com.karamlyy.weather;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String API_KEY = "df07195ad38e7d3cbb2f448d3aec3285";

    Button btnSearch;
    EditText etCityName;
    ImageView iconWeather;
    TextView tvTemp, tvCity;
    ListView lvDailyWeather;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        btnSearch = findViewById(R.id.btnSearch);
        etCityName = findViewById(R.id.etCityName);
        iconWeather = findViewById(R.id.iconWeather);
        tvTemp = findViewById(R.id.tvTemp);
        tvCity = findViewById(R.id.tvCity);
        lvDailyWeather = findViewById(R.id.lvDailyWeather);


        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = etCityName.getText().toString();
                if (city.isEmpty())
                    Toast.makeText(MainActivity.this, "Please enter a city name", Toast.LENGTH_SHORT).show();
                else {
                    // TODO : load weather by city name !
                    loadWeatherByCityName(city);
                }
            }
        });

    }

    private void loadWeatherByCityName(String city) {
        String apiUrl = "http://api.openweathermap.org/data/2.5/weather?q=" + city + "&units=metric&appid=" + API_KEY;
        Ion.with(this)
                .load(apiUrl)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        // do stuff with the result or error

                        if (e != null) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Server error", Toast.LENGTH_SHORT).show();
                        } else {
                            // convert json response to java
                            JsonObject main = result.get("main").getAsJsonObject();
                            double temp = main.get("temp").getAsDouble();
                            tvTemp.setText(temp + "°C");

                            JsonObject sys = result.get("sys").getAsJsonObject();
                            String country = sys.get("country").getAsString();
                            tvCity.setText(city + ", " + country);

                            JsonArray weather = result.get("weather").getAsJsonArray();
                            String icon = weather.get(0).getAsJsonObject().get("icon").getAsString();
                            loadIcon(icon);

                            JsonObject coord = result.get("coord").getAsJsonObject();
                            double lon = coord.get("lon").getAsDouble();
                            double lat = coord.get("lat").getAsDouble();
                            loadDailyForecast(lon, lat);
                        }
                    }
                });
    }

    private void loadDailyForecast(double lon, double lat) {
        String apiUrl = "https://api.openweathermap.org/data/2.5/onecall?lat="+lat+"&lon="+lon+"&exclude=hourly,minutely,current&units=metric&appid=" + API_KEY;
        Ion.with(this)
                .load(apiUrl)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        // do stuff with the result or error
                        if (e != null) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Server error", Toast.LENGTH_SHORT).show();
                        } else {
                            List<Weather> weatherList = new ArrayList<>();
                            String timeZone = result.get("timezone").getAsString();
                            JsonArray daily = result.get("daily").getAsJsonArray();
                            for(int i=1;i<daily.size();i++) {
                                Long date = daily.get(i).getAsJsonObject().get("dt").getAsLong();
                                Double temp = daily.get(i).getAsJsonObject().get("temp").getAsJsonObject().get("day").getAsDouble();
                                String icon = daily.get(i).getAsJsonObject().get("weather").getAsJsonArray().get(0).getAsJsonObject().get("icon").getAsString();
                                weatherList.add(new Weather(date, timeZone, temp, icon));
                            }

                            // attach adapter to listview
                            DailyWeatherAdapter dailyWeatherAdapter = new DailyWeatherAdapter(MainActivity.this, weatherList);
                            lvDailyWeather.setAdapter(dailyWeatherAdapter);
                        }
                    }
                });
    }


    private void loadIcon(String icon) {
        Ion.with(this)
                .load("http://openweathermap.org/img/w/" + icon + ".png")
                .intoImageView(iconWeather);
    }

}