package domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Represents a road in the Smart Traffic system.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Road {
    private String rt;
    private String link;
    private String name;
    private String id;
    private List<RoadSegment> segments;

    // Getters and Setters
    public String getRt() { return rt; }
    public void setRt(String rt) { this.rt = rt; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public List<RoadSegment> getSegments() { return segments; }
    public void setSegments(List<RoadSegment> segments) { this.segments = segments; }

    @Override
    public String toString() {
        return "Road{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", rt='" + rt + '\'' +
                ", segmentsCount=" + (segments != null ? segments.size() : 0) +
                '}';
    }
}
