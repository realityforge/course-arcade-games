package org.realityforge.arcade.racing;

import com.google.gwt.core.client.EntryPoint;
import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.DOMRect;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLCanvasElement;
import elemental2.dom.HTMLHtmlElement;
import elemental2.dom.HTMLImageElement;
import elemental2.dom.KeyboardEvent;
import elemental2.dom.MouseEvent;
import javax.annotation.Nonnull;
import jsinterop.base.Js;

public class Racing
  implements EntryPoint
{
  private static final int WORLD_WIDTH = 800;
  private static final int WORLD_HEIGHT = 600;
  private static final int TRACK_COLUMNS = 20;
  private static final int TRACK_ROWS = 15;
  private static final double TRACK_WIDTH = WORLD_WIDTH * 1D / TRACK_COLUMNS;
  private static final double TRACK_HEIGHT = 40D;
  private static final double TRACK_GAP = 2D;
  private static final int FRAMES_PER_SECOND = 30;
  private static final int MILLIS_PER_SECOND = 1000;
  private static final int FRAME_DELAY = MILLIS_PER_SECOND / FRAMES_PER_SECOND;
  // The world map.
  // 0 - is space
  // 1 - is wall
  // 2 - is starting location
  private static final int[] world = new int[]{
    1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
    1, 0, 0, 0, 0, 0, 0, 0, 1, 2, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1,
    1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1,
    1, 0, 0, 1, 1, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1,
    1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 1, 1, 1, 0, 0, 1,
    1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 1, 1, 1, 0, 0, 1,
    1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 0, 0, 0, 0, 1,
    1, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 0, 0, 0, 0, 1,
    1, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 0, 0, 0, 1, 1,
    1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 0, 0, 0, 0, 1,
    1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 0, 1, 0, 0, 1,
    1, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 1, 0, 0, 0, 0, 1,
    1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 1,
    1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 1,
    1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
    };
  private static final double TURN_RATE = 0.04D;
  private static final double ACCEL_RATE = 0.4D;
  private static final double FRICTION_RATIO = 0.04;
  private HTMLCanvasElement _canvas;
  private CanvasRenderingContext2D _context;
  private HTMLImageElement _carImage;
  private boolean _carImageLoaded;
  private double _carX;
  private double _carY;
  private double _carAngle;
  private double _carSpeed;
  private boolean _simulationActive = true;
  private boolean _showMouseCoords = false;
  private boolean _showTrackCoords = false;
  private boolean _carToMouse = false;
  private boolean _accelerateHeld = false;
  private boolean _brakeHeld = false;
  private boolean _leftHeld = false;
  private boolean _rightHeld = false;
  private double _mouseX;
  private double _mouseY;

  @Override
  public void onModuleLoad()
  {
    prepareCarImage();
    _canvas = (HTMLCanvasElement) DomGlobal.document.createElement( "canvas" );
    _canvas.height = WORLD_HEIGHT;
    _canvas.width = WORLD_WIDTH;
    DomGlobal.document.documentElement.appendChild( _canvas );
    _context = Js.uncheckedCast( _canvas.getContext( "2d" ) );

    _canvas.addEventListener( "mousemove", e -> calculateMousePosition( (MouseEvent) e ) );
    DomGlobal.document.addEventListener( "keydown", e -> onKeyPress( (KeyboardEvent) e ) );
    DomGlobal.document.addEventListener( "keyup", e -> onKeyRelease( (KeyboardEvent) e ) );

    resetGame();

    runFrame();
    DomGlobal.setInterval( v -> runFrame(), FRAME_DELAY );
  }

  private void prepareCarImage()
  {
    _carImage = (HTMLImageElement) DomGlobal.document.createElement( "img" );
    _carImage.onload = e -> {
      _carImageLoaded = true;
      //Fix this after Elemental2 is fixed
      return null;
    };
    _carImage.src = "car.png";
  }

  private void onKeyPress( @Nonnull final KeyboardEvent event )
  {
    if ( " ".equals( event.key ) )
    {
      _simulationActive = !_simulationActive;
    }
    // the 1 key turns on debugging of mouse coordinates
    else if ( "1".equals( event.key ) )
    {
      _showMouseCoords = !_showMouseCoords;
      _showTrackCoords = false;
    }
    // the 2 key turns on debugging in track coordinates
    else if ( "2".equals( event.key ) )
    {
      _showTrackCoords = !_showTrackCoords;
      _showMouseCoords = false;
    }
    // the 3 key instantly transports car to mouse and changes direction to left direction
    else if ( "3".equals( event.key ) )
    {
      _carX = _mouseX;
      _carY = _mouseY;
      _carAngle = 0;
      _carSpeed = 0;
    }
    // the 4 key transports car to mouse when the mouse moves
    else if ( "4".equals( event.key ) )
    {
      _carToMouse = !_carToMouse;
    }
    else if ( "ArrowLeft".equals( event.code ) )
    {
      _leftHeld = true;
    }
    else if ( "ArrowRight".equals( event.code ) )
    {
      _rightHeld = true;
    }
    else if ( "ArrowUp".equals( event.code ) )
    {
      _accelerateHeld = true;
    }
    else if ( "ArrowDown".equals( event.code ) )
    {
      _brakeHeld = true;
    }
    else
    {
      return;
    }
    event.preventDefault();
  }

  private void onKeyRelease( @Nonnull final KeyboardEvent event )
  {
    if ( "ArrowLeft".equals( event.code ) )
    {
      _leftHeld = false;
    }
    else if ( "ArrowRight".equals( event.code ) )
    {
      _rightHeld = false;
    }
    else if ( "ArrowUp".equals( event.code ) )
    {
      _accelerateHeld = false;
    }
    else if ( "ArrowDown".equals( event.code ) )
    {
      _brakeHeld = false;
    }
    else
    {
      return;
    }
    event.preventDefault();
  }

  @SuppressWarnings( { "unused" } )
  private void calculateMousePosition( @Nonnull final MouseEvent event )
  {
    final DOMRect rect = _canvas.getBoundingClientRect();
    final HTMLHtmlElement root = DomGlobal.document.documentElement;

    // The clientX/clientY properties are the coordinates relative to the client area of the mouse
    // pointer when a mouse event was triggered.. The client area is the current window.
    // Thus translating it according to component and scrolling will get coordinate within component.
    _mouseX = event.clientX - rect.x - root.scrollLeft;
    _mouseY = event.clientY - rect.top - root.scrollTop;

    if ( _carToMouse )
    {
      _carX = _mouseX;
      _carY = _mouseY;
      _carSpeed = 0;
      _carAngle = 0;
    }
  }

  private void runFrame()
  {
    if ( _simulationActive )
    {
      simulateWorld();
    }
    renderWorld();
  }

  private void simulateWorld()
  {
    moveCar();

    carTrackCollisionDetection();
  }

  private void moveCar()
  {
    _carSpeed *= (1.0 - FRICTION_RATIO) ;
    if ( _leftHeld )
    {
      _carAngle -= TURN_RATE;
    }
    if ( _rightHeld )
    {
      _carAngle += TURN_RATE;
    }
    if ( _accelerateHeld )
    {
      _carSpeed += ACCEL_RATE;
    }
    if ( _brakeHeld )
    {
      _carSpeed -= ACCEL_RATE;
    }

    _carX += Math.cos( _carAngle ) * _carSpeed;
    _carY += Math.sin( _carAngle ) * _carSpeed;
  }

  private void carReset()
  {
    _carSpeed = 0;
    _carAngle = Math.PI * 0.5D;

    for ( int i = 0; i < TRACK_COLUMNS; i++ )
    {
      for ( int j = 0; j < TRACK_ROWS; j++ )
      {
        if ( 2 == world[ trackIndex( i, j ) ] )
        {
          _carX = i * TRACK_WIDTH + ( TRACK_WIDTH / 2 );
          _carY = j * TRACK_HEIGHT + ( TRACK_HEIGHT / 2 );
          break;
        }
      }
    }
  }

  private void carTrackCollisionDetection()
  {
    final int carTrackCol = toTrackColumn( _carX );
    final int carTrackRow = toTrackRow( _carY );
    if ( isValidTrackCoordinates( carTrackCol, carTrackRow ) )
    {
      if ( 1 == world[ trackIndex( carTrackCol, carTrackRow ) ] )
      {
        // This is to reverse action of frame to avoid car getting stuck in the wall before we reverse direction
        // otherwise next frame could see car try to reverse out when inside the wall and not make it out
        _carX -= Math.cos( _carAngle ) * _carSpeed;
        _carY -= Math.sin( _carAngle ) * _carSpeed;

        // The bounce saps some energy
        _carSpeed = 0.3 * -_carSpeed;
      }
    }
  }

  private void resetGame()
  {
    carReset();
  }

  private void renderWorld()
  {
    // Background
    clearBackground();

    drawTracks();

    drawCar();

    if ( _showMouseCoords )
    {
      drawText( _mouseX, _mouseY, _mouseX + "," + _mouseY, "yellow" );
    }
    else if ( _showTrackCoords )
    {
      final double trackCol = toTrackColumn( _mouseX );
      final double trackRow = toTrackRow( _mouseY );
      if ( isValidTrackCoordinates( trackCol, trackRow ) )
      {
        drawText( _mouseX, _mouseY, Math.floor( trackCol ) + "," + Math.floor( trackRow ), "yellow" );
      }
    }
  }

  private void drawCar()
  {
    if ( _carImageLoaded )
    {
      drawImageWithRotation( _carImage, _carX, _carY, _carAngle );
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

  private boolean isValidTrackCoordinates( final double trackCol, final double trackRow )
  {
    return trackCol >= 0 && trackCol < TRACK_COLUMNS && trackRow >= 0 && trackRow < TRACK_ROWS;
  }

  private int toTrackRow( final double mouseY )
  {
    return (int) Math.floor( mouseY / TRACK_HEIGHT );
  }

  private int toTrackColumn( final double x )
  {
    return (int) Math.floor( x / TRACK_WIDTH );
  }

  private void drawTracks()
  {
    for ( int i = 0; i < TRACK_ROWS; i++ )
    {
      final double rowY = i * TRACK_HEIGHT;
      for ( int j = 0; j < TRACK_COLUMNS; j++ )
      {
        if ( 1 == world[ trackIndex( j, i ) ] )
        {
          drawRect( TRACK_WIDTH * j, rowY, TRACK_WIDTH - TRACK_GAP, TRACK_HEIGHT - TRACK_GAP, "blue" );
        }
      }
    }
  }

  private int trackIndex( final int column, final int row )
  {
    return row * TRACK_COLUMNS + column;
  }

  private void clearBackground()
  {
    drawRect( 0D, 0D, _canvas.width, _canvas.height, "black" );
  }

  @SuppressWarnings( "SameParameterValue" )
  private void drawText( final double bottomLeftX,
                         final double bottomLeftY,
                         @Nonnull final String text,
                         @Nonnull final String color )
  {
    _context.fillStyle = CanvasRenderingContext2D.FillStyleUnionType.of( color );
    _context.fillText( text, bottomLeftX, bottomLeftY );
  }

  @SuppressWarnings( "SameParameterValue" )
  private void drawRect( final double topLeftX,
                         final double topLeftY,
                         final double width,
                         final double height,
                         @Nonnull final String color )
  {
    _context.fillStyle = CanvasRenderingContext2D.FillStyleUnionType.of( color );
    _context.fillRect( topLeftX, topLeftY, width, height );
  }
}
