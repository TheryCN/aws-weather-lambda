console.log('Loading function');

/* Map OpenWeatherMap currentWeather data to our format */
const map = (record) => {
  console.log("Map record");
  /* Data is base64-encoded, so decode here */
  let recordedData = JSON.parse(Buffer.from(record.data, 'base64').toString('ascii'));
  let processedData = {
    coord: {
      lon: recordedData.coord.lon,
      lat: recordedData.coord.lat
    },
    weather: {
      title: recordedData.weather.map(element => element.main).join(","),
      description: recordedData.weather.map(element => element.description).join(","),
      temp: recordedData.main.temp,
      feels_like: recordedData.main.feels_like,
      temp_min: recordedData.main.temp_min,
      temp_max: recordedData.main.temp_max,
      pressure: recordedData.main.pressure,
      humidity: recordedData.main.humidity,
      wind_speed: recordedData.wind.speed,
      wind_deg: recordedData.wind.deg,
      clouds: recordedData.clouds.all
    },
    datetime: recordedData.dt
  };

  /* Encode decompressed JSON */
  return {
    recordId: record.recordId,
    result: 'Ok',
    data: Buffer.from(JSON.stringify(processedData) + '\n').toString('base64'),
  };
};

exports.handler = async (event, context) => {
  /* Process the list of records and transform them */
  const output = event.records.map((record) => map(record));
  console.log(`Processing completed.  Successful records ${output.length}.`);
  return {
    records: output
  };
};
