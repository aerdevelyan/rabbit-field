import _ from 'lodash';
import FieldModel from './FieldModel';

/**
 * Main application class.
 */
export default class AppMain  {
    
    constructor() {
        this.fieldModel = new FieldModel();
        
    }
    
    start() {
        console.log("Starting application.");
        this.fieldModel.initCellsAndTable();
        this.connectWS();
    }
    	
    connectWS() {
        // Create WebSocket connection.
        const socket = new WebSocket('ws://localhost:8080/ws');

        // Connection opened
        socket.addEventListener('open', function(event) {
            socket.send('Hello Server!');
        });

        let fm = this.fieldModel
        // Listen for messages
        socket.addEventListener('message', function(event) {
            console.log('Message from server ', event.data);
            fm.display(JSON.parse(event.data));
        });
        
    }
}

// make it available on the web page 
window.AppMain = AppMain;
