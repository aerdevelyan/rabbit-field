import _ from 'lodash';
import FieldModel from './FieldModel';

/**
 * rabbits app
 */
export default class AppMain  {
    
    constructor() {
        this.fieldModel = new FieldModel();
        
    }
    
    start() {
        console.log("Starting application.");
        this.fieldModel.initCells();
        this.connectWS();
    }
    
	/**
	 * just a func
	 * @param {numeric} p1 parameter one
	 * @returns {String}
	 */
	fn1(p1) {
		return "fn1 called with " + p1;
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

window.AppMain = AppMain;
