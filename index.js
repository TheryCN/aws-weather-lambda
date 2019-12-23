var http = require('http');

// Load the AWS SDK
var AWS = require('aws-sdk'),
  region = "eu-west-3",
  secretName = "OpenWeatherMapApplicationId";

// Create a Secrets Manager client
var client = new AWS.SecretsManager({
  region: region
});

// Create a Kinesis Firehose client
var firehoseClient = new AWS.Firehose();

// Read secret from AWS Secrets Manager
const getSecret = () => {
  return new Promise((resolve, reject) => {
    client.getSecretValue({
      SecretId: secretName
    }, function(err, data) {
      if (err) {
        reject(err);
      } else {
        if ('SecretString' in data) {
          resolve(data.SecretString);
        } else {
          let buff = new Buffer(data.SecretBinary, 'base64');
          resolve(buff.toString('ascii'));
        }
      }
    });
  });
};

// Fetch weather from OpenWeatherMap
const fetchWeather = (AppId, city) => {
  return new Promise((resolve, reject) => {
    const request = http.get('http://api.openweathermap.org/data/2.5/weather?q=' + city + '&units=metric&appid=' + AppId,
      function(response) {
        // Continuously update stream with data
        var body = '';
        response.on('data', (chunk) => body += chunk);
        response.on('end', () => resolve(JSON.parse(body)));
      });
    // handle connection errors of the request
    request.on('error', (err) => reject(err))
  });
};

// Send data to AWS Kinesis Firehose
const sendToFirehose = (deliveryStreamName, record) => {
  return new Promise((resolve, reject) => {
    firehoseClient.putRecord({
        DeliveryStreamName: deliveryStreamName,
        Record: {
          Data: JSON.stringify(record)
        }
      },
      function(err, data) {
        if (err) {
          reject(err);
        }
        resolve(data);
      });
  });
};

exports.handler = async (event) => {
  try {
    var secret = await getSecret();
    // event.queryStringParameters for query parameters or event.pathParameters for path parameters
    let city = (event.queryStringParameters && event.queryStringParameters.city) ? event.queryStringParameters.city : "Grenoble";
    let weatherResponse = await fetchWeather(JSON.parse(secret).appId, city);
    let firehoseResponse = await sendToFirehose('ty-weather-flow', weatherResponse);

    // return body must be stringify
    return {
      "statusCode": 200,
      "headers": {
        "Content-Type": "application/json"
      },
      "body": JSON.stringify(firehoseResponse)
    };
  } catch (e) {
    console.error(e);
  }
};
