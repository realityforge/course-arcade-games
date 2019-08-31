package org.realityforge.arcade.racing;

import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLCanvasElement;
import elemental2.dom.HTMLImageElement;
import javax.annotation.Nonnull;
import jsinterop.base.Js;

final class Renderer
{
  private static final int WORLD_WIDTH = 800;
  private static final int WORLD_HEIGHT = 600;
  @Nonnull
  private final HTMLCanvasElement _canvas;
  @Nonnull
  private final CanvasRenderingContext2D _context;

  Renderer()
  {
    _canvas = (HTMLCanvasElement) DomGlobal.document.createElement( "canvas" );
    _canvas.height = WORLD_HEIGHT;
    _canvas.width = WORLD_WIDTH;
    DomGlobal.document.documentElement.appendChild( _canvas );
    _context = Js.uncheckedCast( _canvas.getContext( "2d" ) );
  }

  @Nonnull
  HTMLCanvasElement getCanvas()
  {
    return _canvas;
  }

  void drawBody( @Nonnull final Body body )
  {
    if ( body.isImageLoaded() )
    {
      drawImageWithRotation( body.getImage(), body.getX(), body.getY(), body.getAngle() );
    }
  }

  private void drawImageWithRotation( @Nonnull final HTMLImageElement image,
                                      final double centerX,
                                      final double centerY,
                                      final double angleInRadians )
  {
    // Save the context and push it onto stack
    // This is presumable rotation matrix and friends although unclear exactly what is included)
    _context.save();

    _context.translate( centerX, centerY );
    _context.rotate( angleInRadians );

    // X/Y indicate center where drawImage is top left corner
    _context.drawImage( image, -image.width / 2D, -image.height / 2D );

    // Pop state to return to transform matrix prior to method call
    _context.restore();
  }

  void drawImage( @Nonnull final HTMLImageElement image, final double topX, final double topY )
  {
    _context.drawImage( image, topX, topY );
  }

  void clearBackground()
  {
    drawRect( 0D, 0D, _canvas.width, _canvas.height, "black" );
  }

  @SuppressWarnings( "SameParameterValue" )
  void drawText( final double bottomLeftX,
                 final double bottomLeftY,
                 @Nonnull final String text,
                 @Nonnull final String color )
  {
    _context.fillStyle = CanvasRenderingContext2D.FillStyleUnionType.of( color );
    _context.fillText( text, bottomLeftX, bottomLeftY );
  }

  @SuppressWarnings( "SameParameterValue" )
  void drawRect( final double topLeftX,
                 final double topLeftY,
                 final double width,
                 final double height,
                 @Nonnull final String color )
  {
    _context.fillStyle = CanvasRenderingContext2D.FillStyleUnionType.of( color );
    _context.fillRect( topLeftX, topLeftY, width, height );
  }
}
