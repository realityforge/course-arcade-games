package org.realityforge.arcade.racing;

import com.google.gwt.core.client.EntryPoint;
import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.DOMRect;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLCanvasElement;
import elemental2.dom.HTMLHtmlElement;
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
  private static final double BALL_RADIUS = 10D;
  private static final double MAX_INITIAL_X_SPEED = 8D;
  private static final double MIN_INITIAL_X_SPEED = 0.5D;
  private static final double MAX_INITIAL_Y_SPEED = 12D;
  private static final double MIN_INITIAL_Y_SPEED = 7D;

  // The world map.
  // 0 - is space
  // 1 - is wall
  // 2 - is starting location
  private static final int[] trackGrid = new int[]{
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
  private HTMLCanvasElement _canvas;
  private CanvasRenderingContext2D _context;
  private double _ballX;
  private double _ballY;
  private double _ballSpeedX;
  private double _ballSpeedY;
  private boolean _simulationActive = true;
  private boolean _showMouseCoords = false;
  private boolean _showTrackCoords = false;
  private boolean _ballToMouseLeft = false;
  private boolean _ballToMouseRight = false;
  private double _mouseX;
  private double _mouseY;

  @Override
  public void onModuleLoad()
  {
    _canvas = (HTMLCanvasElement) DomGlobal.document.createElement( "canvas" );
    _canvas.height = WORLD_HEIGHT;
    _canvas.width = WORLD_WIDTH;
    DomGlobal.document.documentElement.appendChild( _canvas );
    _context = Js.uncheckedCast( _canvas.getContext( "2d" ) );

    _canvas.addEventListener( "mousemove", e -> calculateMousePosition( (MouseEvent) e ) );
    DomGlobal.document.addEventListener( "keydown", e -> onKeyPress( (KeyboardEvent) e ) );

    resetGame();

    runFrame();
    DomGlobal.setInterval( v -> runFrame(), FRAME_DELAY );
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
    // the 3 key instantly transports ball to mouse and changes direction to left direction
    else if ( "3".equals( event.key ) )
    {
      _ballX = _mouseX;
      _ballY = _mouseY;
      _ballSpeedX = 4D;
      _ballSpeedY = -4D;
    }
    // the 4 key instantly transports ball to mouse and changes direction to right direction
    else if ( "4".equals( event.key ) )
    {
      _ballX = _mouseX;
      _ballY = _mouseY;
      _ballSpeedX = -4D;
      _ballSpeedY = -4D;
    }
    // the 5 key transports ball to mouse when the mouse moves and changes direction to left direction
    // the control is a toggle
    else if ( "5".equals( event.key ) )
    {
      _ballToMouseLeft = !_ballToMouseLeft;
      if ( _ballToMouseLeft )
      {
        _ballToMouseRight = false;
      }
    }
    // the 6 key transports ball to mouse when the mouse moves and changes direction to left direction
    // the control is a toggle
    else if ( "6".equals( event.key ) )
    {
      _ballToMouseRight = !_ballToMouseRight;
      if ( _ballToMouseRight )
      {
        _ballToMouseLeft = false;
      }
    }
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

    if ( _ballToMouseLeft )
    {
      _ballX = _mouseX;
      _ballY = _mouseY;
      _ballSpeedX = -4D;
      _ballSpeedY = -4D;
    }
    else if ( _ballToMouseRight )
    {
      _ballX = _mouseX;
      _ballY = _mouseY;
      _ballSpeedX = 4D;
      _ballSpeedY = -4D;
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
    moveBall();

    ballTrackCollisionDetection();
  }

  private void moveBall()
  {
    _ballX += _ballSpeedX;
    _ballY += _ballSpeedY;

    final double ballTopY = _ballY - BALL_RADIUS;
    final double ballBottomY = _ballY + BALL_RADIUS;
    final double ballLeftX = _ballX - BALL_RADIUS;
    final double ballRightX = _ballX + BALL_RADIUS;

    // Bounce off the top edge
    if ( ballTopY < 0 )
    {
      // Ensure that if the ball is coming down because it has somehow got above
      // the world then let it continue coming down
      if ( _ballSpeedY < 0 )
      {
        _ballSpeedY = -_ballSpeedY;
      }
    }
    // Reset game if missed and ball falls off the bottom edge
    else if ( ballBottomY > _canvas.height )
    {
      resetGame();
    }
    // Bounce off the side edges
    else if ( ballRightX > _canvas.width )
    {
      // If the ball is outside the world coming in then let
      // it, otherwise reverse it back towards the world.
      // Sometimes the paddle will jump it outside the world
      if ( _ballSpeedX > 0 )
      {
        _ballSpeedX = -_ballSpeedX;
      }
    }
    else if ( ballLeftX < 0 )
    {
      // If the ball is outside the world coming in then let
      // it, otherwise reverse it back towards the world.
      // Sometimes the paddle will jump it outside the world
      if ( _ballSpeedX < 0 )
      {
        _ballSpeedX = -_ballSpeedX;
      }
    }
  }

  private void ballReset()
  {
    _ballSpeedX = ( Math.random() < 0.5 ? -1D : 1D ) * randomValue( MIN_INITIAL_X_SPEED, MAX_INITIAL_X_SPEED );
    _ballSpeedY = randomValue( MIN_INITIAL_Y_SPEED, MAX_INITIAL_Y_SPEED );

    for ( int i = 0; i < TRACK_COLUMNS; i++ )
    {
      for ( int j = 0; j < TRACK_ROWS; j++ )
      {
        if ( 2 == trackGrid[ trackIndex( i, j ) ] )
        {
          _ballX = i * TRACK_WIDTH + ( TRACK_WIDTH / 2 );
          _ballY = j * TRACK_HEIGHT + ( TRACK_HEIGHT / 2 );
          break;
        }
      }
    }
  }

  private void ballTrackCollisionDetection()
  {
    final int ballTrackCol = toTrackColumn( _ballX );
    final int ballTrackRow = toTrackRow( _ballY );
    if ( isValidTrackCoordinates( ballTrackCol, ballTrackRow ) )
    {
      if ( 0 != trackGrid[ trackIndex( ballTrackCol, ballTrackRow ) ] )
      {
        final int prevBallTrackCol = toTrackColumn( _ballX - _ballSpeedX );
        final int prevBallTrackRow = toTrackRow( _ballY - _ballSpeedY );
        if ( prevBallTrackCol != ballTrackCol )
        {
          if (
            // Don't reflect if we hit a horizontal surface
            0 == trackGrid[ trackIndex( prevBallTrackCol, ballTrackRow ) ] ||

            // This next condition handles the scenario where hit inside corner where we still want to reverse
            1 == trackGrid[ trackIndex( ballTrackCol, prevBallTrackRow ) ] )
          {
            _ballSpeedX = -_ballSpeedX;
          }
        }
        if ( prevBallTrackRow != ballTrackRow )
        {
          if (
            // Don't reflect if we hit a vertical surface
            0 == trackGrid[ trackIndex( ballTrackCol, prevBallTrackRow ) ] ||

            // This next condition handles the scenario where hit inside corner where we still want to reverse
            1 == trackGrid[ trackIndex( prevBallTrackCol, ballTrackRow ) ] )
          {
            _ballSpeedY = -_ballSpeedY;
          }
        }
      }
    }
  }

  private void resetGame()
  {
    ballReset();
  }

  private double randomValue( final double min, final double max )
  {
    return ( Math.random() * ( max - min ) ) + min;
  }

  private void renderWorld()
  {
    // Background
    clearBackground();

    drawTracks();

    // Ball
    drawCircle( _ballX, _ballY, BALL_RADIUS, "red" );

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
        if ( 1 == trackGrid[ trackIndex( j, i ) ] )
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
  private void drawCircle( final double centerX,
                           final double centerY,
                           final double radius,
                           @Nonnull final String color )
  {
    _context.fillStyle = CanvasRenderingContext2D.FillStyleUnionType.of( color );
    _context.beginPath();
    _context.arc( centerX, centerY, radius, 0, Math.PI * 2 );
    _context.fill();
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
