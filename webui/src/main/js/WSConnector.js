/**
 * WebSocket connection to server.
 */
export default class WSConnector {
    constructor() {
        this.fieldModel;
        this.socket;
    }
    
    connect() {
        // Create WebSocket connection.
        this.socket = new WebSocket('ws://localhost:8080/ws');

        // Connection opened
        this.socket.addEventListener('open', event => {
//            this.socket.send('Hello Server!');
            console.log('Opened WS connection to the server.');
        });

        // Listen for messages
//        let fm = this.fieldModel
        this.socket.addEventListener('message', event => {
            console.log('Message from server ', event.data);
            this.fieldModel.display(JSON.parse(event.data));
        });
        
    }

    sendMsg(msg) {
        this.socket.send(JSON.stringify(msg));
    }
    
}