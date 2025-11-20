package main;

import java.awt.Rectangle;

public class EventRect extends Rectangle {

  int eventRectDefaultX;
  int eventRectDefaultY;
  boolean eventDone = false;

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    if (!super.equals(obj)) {
      return false;
    }

    EventRect other = (EventRect) obj;

    return this.eventRectDefaultX == other.eventRectDefaultX
        && this.eventRectDefaultY == other.eventRectDefaultY
        && this.eventDone == other.eventDone;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + eventRectDefaultX;
    result = 31 * result + eventRectDefaultY;
    result = 31 * result + (eventDone ? 1 : 0);
    return result;
  }
}
