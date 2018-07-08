

/**
 * Listens to events from field controls.
 */
export default class FieldControls {
    constructor() {
        this.pause = true;
        this.wsConnector;
        this.initListeners();
    }
    
    initListeners() {
        document.getElementById("btnPauseResume").addEventListener('click', () => this.pauseResumeToggle());
        document.getElementById("btnShutdown").addEventListener('click', () => this.shutdown());
    }
    
    pauseResumeToggle() {
        this.pause = !this.pause;
        let pauseResumeMsg = {
            type: 'PAUSE_RESUME',
            pause: this.pause
        };
        console.log('Pause/Resume toggle', pauseResumeMsg);
        this.wsConnector.sendMsg(pauseResumeMsg);
    }
    
    shutdown() {
        let shutdownMsg = {
            type: 'SHUTDOWN'
        }
        console.log('Sending shutdown request', shutdownMsg);
        this.wsConnector.sendMsg(shutdownMsg);
    }
}
