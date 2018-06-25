package rabbit_field.msg;

import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.bind.adapter.JsonbAdapter;

import rabbit_field.field.CellView;

/**
 * Represents a view of all non-empty field cells at some moment of time.
 * Example of serialized form:
 * {"type":"FIELD_VIEW","cells":[{"fo":["r","ca"],"hpos":49,"vpos":33},{"fo":["r"],"hpos":17,"vpos":41}]}
 */
public class FieldViewMsg extends Message {
	private List<CellView> cells;

	public FieldViewMsg(List<CellView> cells) {
		setType(MsgType.FIELD_VIEW);
		this.cells = cells;
	}

	public List<CellView> getCells() {
		return cells;
	}

	public void setCells(List<CellView> cells) {
		this.cells = cells;
	}
	
	public static class CellViewAdapter implements JsonbAdapter<CellView, JsonObject> {
		@Override
		public JsonObject adaptToJson(CellView cv) throws Exception {
			JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
			cv.getFobjects().forEach(fov -> arrayBuilder.add(fov.getCode()));
			JsonObjectBuilder objectBuilder = Json.createObjectBuilder()
					.add("fo", arrayBuilder.build())
					.add("hpos", cv.getPosition().getHpos())
					.add("vpos", cv.getPosition().getVpos());
			return objectBuilder.build();
		}

		@Override
		public CellView adaptFromJson(JsonObject obj) throws Exception {
			throw new UnsupportedOperationException("FieldViewMsg cannot be deserialized.");
		}
	}
}
