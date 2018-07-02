//import _ from 'lodash';
import FieldModel from './FieldModel';
import FieldControls from './FieldControls';
import WSConnector from './WSConnector';

/**
 * Main application class.
 */
export default class AppMain  {
    
    constructor() {
        this.fieldModel = new FieldModel();
        this.fieldControls = new FieldControls();
        this.wsConnector = new WSConnector();
        this.setDependencies();
    }
    
    setDependencies() {
        this.wsConnector.fieldModel = this.fieldModel;
        this.fieldControls.wsConnector = this.wsConnector;
    }
    
    start() {
        console.log("Starting application.");
        this.fieldModel.initCellsAndTable();
        this.wsConnector.connect();
    }

}

// make it available on the web page 
window.AppMain = AppMain;
