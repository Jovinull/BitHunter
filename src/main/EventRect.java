package main;

import java.awt.Rectangle;

public class EventRect extends Rectangle {

  int eventRectDefaultX;
  int eventRectDefaultY;
  boolean eventDone = false;

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!(obj instanceof EventRect))
      return false;
    if (!super.equals(obj))
      return false;
    EventRect other = (EventRect) obj;
    return this.eventRectDefaultX == other.eventRectDefaultX
        && this.eventRectDefaultY == other.eventRectDefaultY
        && this.eventDone == other.eventDone;
  }
}
