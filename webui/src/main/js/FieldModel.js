import '../main.css';
import RabbitPng from '../img/rabbit.png';
import CloverPng from '../img/clover.png';
import CarrotPng from '../img/carrot.png';

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
            cell.fos = cv.fo;
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
        this._fos = [];
        this.container = container;
    }
    
    set fos(fos) {
        this._fos = fos;
        if (fos.length > 0) {
            let topFO = this.findTopFO(fos);
            let foIcon = this.findIconFor(topFO);
            if (this.container.children.length == 0) {
                this.container.appendChild(foIcon); 
            }
        }
        else {
            while (this.container.firstChild) { 
                this.container.removeChild(this.container.firstChild); 
            }
        }
    }
    
    clear() {
        this.fos = [];
    }
    
    findIconFor(fo) {
        let icon = new Image();
        icon.src = iconMap[fo];
        return icon;
    }
    
    findTopFO(fos) {
        fos.sort((fo1, fo2) => {
            return iconPriorityMap[fo2] - iconPriorityMap[fo1];
        });
        return fos[0];
    }
}

const iconMap = {
    'r': RabbitPng, 'cl': CloverPng, 'ca': CarrotPng
};

const iconPriorityMap = {
    'cl': 1, 'ca': 1, 'r': 2, 'f': 3
};

