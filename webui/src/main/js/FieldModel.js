import '../main.css';
import RabbitPng from '../img/rabbit.png';

/**
 * Display model for field view data received from server.
 */
export default class FieldModel {
    constructor() {
        this.width = 40;
        this.height = 25;
        this.cells = [];
        this.prevFVData;
    }
    
    initCellsAndTable() {        
        const tbl = document.createElement("table");
        document.getElementById("field").appendChild(tbl);
        tbl.classList.add('field');
        
        for (let vidx = 0; vidx < this.height; vidx++) {
            let tr = document.createElement("tr");
            tbl.appendChild(tr);
            this.cells[vidx] = [];
            for (let hidx = 0; hidx < this.width; hidx++) {
                let td = document.createElement("td");
                tr.appendChild(td);
                this.cells[vidx][hidx] = new Cell(td);
            }
        }
    }
    
    display(fieldViewData) {
        if (this.prevFVData) {
            this.forEachCellView(this.prevFVData, (cv, cell) => {
                cell.clear();
            });
        }
        this.forEachCellView(fieldViewData, (cv, cell) => {
            cell.fo = cv.fo;
        });
        this.prevFVData = fieldViewData;
    }
    
    forEachCellView(fieldViewData, callback) {
        fieldViewData.cells.forEach(cv => {
            let cell = this.cells[cv.vpos][cv.hpos];
            callback(cv, cell, cv.vpos, cv.hpos);
        });
    }
}

class Cell {
    constructor(container) {
        this.picture;
        this._fo = [];
        this.container = container;
    }
    
    set fo(fo) {
        this._fo = fo;
        if (fo.length > 0) {
            let rabbitIcon = new Image();
            rabbitIcon.src = RabbitPng;
            if (this.container.children.length == 0) {
                this.container.appendChild(rabbitIcon); 
            }            
        }
        else {
            while (this.container.firstChild) { 
                this.container.removeChild(this.container.firstChild); 
            }
        }
    }
    
    clear() {
        this.fo = [];
    }
}

