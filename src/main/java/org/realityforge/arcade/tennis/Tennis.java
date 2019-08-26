package org.realityforge.arcade.tennis;

import com.google.gwt.core.client.EntryPoint;
import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLCanvasElement;
import jsinterop.base.Js;

public class Tennis
  implements EntryPoint
{
  @Override
  public void onModuleLoad()
  {
    final HTMLCanvasElement canvas = (HTMLCanvasElement) DomGlobal.document.getElementById( "gameCanvas" );
    final CanvasRenderingContext2D context = Js.uncheckedCast( canvas.getContext( "2d" ) );
  }
}
