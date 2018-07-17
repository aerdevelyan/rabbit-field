

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
        document.getElementById("btnReset").addEventListener('click', () => this.reset());
        document.getElementById("btnShutdown").addEventListener('click', () => this.shutdown());
    }
    
    pauseResumeToggle() {
        this.pause = !this.pause;
        let pauseResumeMsg = {
            type: 'PAUSE_RESUME',
            pause: this.pause
        };
        console.log('Senging Pause/Resume toggle request', pauseResumeMsg);
        this.wsConnector.sendMsg(pauseResumeMsg);
    }
    
    reset() {
        let resetMsg = {
            type: 'RESET'
        }
        console.log('Sending reset request', resetMsg);
        this.wsConnector.sendMsg(resetMsg);        
        if (!this.pause) this.pause = true;
    }
    
    shutdown() {
        let shutdownMsg = {
            type: 'SHUTDOWN'
        }
        console.log('Sending shutdown request', shutdownMsg);
        this.wsConnector.sendMsg(shutdownMsg);
    }
}
