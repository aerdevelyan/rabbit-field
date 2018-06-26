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
    }
    
    initCells() {
        this.createTable();
        
    }
    
    createTable() {
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
    
    display(data) {
        data.cells.forEach(cv => {
            let cell = this.cells[cv.vpos][cv.hpos];
            cell.fo = cv.fo;
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
        let rabbitIcon = new Image();
        rabbitIcon.src = RabbitPng;
        if (this.container.children.length == 0) {
            this.container.appendChild(rabbitIcon); 
        }
    }
}
