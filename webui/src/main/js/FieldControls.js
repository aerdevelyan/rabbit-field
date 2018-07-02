

/**
 * Field controls.
 */
export default class FieldControls {
    constructor() {
        this.pause = true;
        this.wsConnector;
        this.initListeners();
    }
    
    initListeners() {
        document.getElementById("btnPauseResume").addEventListener('click', () => this.pauseResumeToggle());
    }
    
    pauseResumeToggle() {
        this.pause = !this.pause;
        let pauseResumeMsg = {
            type: 'PAUSE_RESUME',
            pause: this.pause
        };
        console.log('PR toggle', pauseResumeMsg);
        this.wsConnector.sendMsg(pauseResumeMsg);
        
    }
}
