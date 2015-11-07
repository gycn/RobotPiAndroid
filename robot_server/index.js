var port = 5000
var host = "localhost"

var socket = net.createConnection(port,host)
console.log("Socket up!")

socket.on('data', function(data){
  console.log('Response: ' + data);
}).on('connection', function(){
  console.log('Connection start!')
}).on('end', function(){
  console.log('')
})