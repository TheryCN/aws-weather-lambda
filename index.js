var http = require('http');

const APP_ID = "YOUR_OPENWEATHERMAP_APP_ID";
const shouldIDoSport = (weatherMain) => weatherMain.temp_min > 10 && weatherMain.temp_max < 28;

// Fetch weather from OpenWeatherMap
const fetchWeather = (city) => {
  return new Promise((resolve, reject) => {
    const request = http.get('http://api.openweathermap.org/data/2.5/weather?q=' + city + '&units=metric&appid=' + APP_ID,
      function(response) {
        // Continuously update stream with data
        var body = '';
        response.on('data', (chunk) => body += chunk);
        response.on('end', () => resolve(JSON.parse(body)));
      });
    // handle connection errors of the request
    request.on('error', (err) => reject(err))
  });
}

exports.handler = async (event) => {
  // event.queryStringParameters for query parameters or event.pathParameters for path parameters
  let city = (event.queryStringParameters && event.queryStringParameters.city) ? event.queryStringParameters.city : "Grenoble";
  let weatherResponse = await fetchWeather(city);
  let state = "The weather in " + weatherResponse.name + " has " + weatherResponse.weather.map(weather => weather.main).join(" and");
  let randomTemperature = Math.floor(Math.random() * (weatherResponse.main.temp_max - weatherResponse.main.temp_min)) + weatherResponse.main.temp_min;

  // return body must be stringify
  return {
    "statusCode": 200,
    "headers": {
      "Content-Type": "application/json"
    },
    "body": JSON.stringify({
      "city": city,
      "state": state,
      "temp_max": weatherResponse.main.temp_max,
      "temp_min": weatherResponse.main.temp_min,
      "temp_random": Math.round(randomTemperature),
      "should_i_do_sport": shouldIDoSport(weatherResponse.main)
    })
  };
};
