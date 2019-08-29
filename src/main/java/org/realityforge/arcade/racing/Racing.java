package org.realityforge.arcade.racing;

import com.google.gwt.core.client.EntryPoint;
import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.DOMRect;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLCanvasElement;
import elemental2.dom.HTMLHtmlElement;
import elemental2.dom.KeyboardEvent;
import elemental2.dom.MouseEvent;
import java.util.Arrays;
import javax.annotation.Nonnull;
import jsinterop.base.Js;

public class Racing
  implements EntryPoint
{
  private static final int WORLD_WIDTH = 800;
  private static final int WORLD_HEIGHT = 600;
  private static final int BRICKS_PER_ROW = 8;
  private static final int BRICK_ROWS = 14;
  private static final double BRICK_WIDTH = WORLD_WIDTH * 1D / BRICKS_PER_ROW;
  private static final double BRICK_HEIGHT = 20D;
  private static final double BRICK_GAP = 2D;
  private static final double SPACE_ABOVE_BRICKS = BRICK_HEIGHT * 3;
  private static final int FRAMES_PER_SECOND = 30;
  private static final int MILLIS_PER_SECOND = 1000;
  private static final int FRAME_DELAY = MILLIS_PER_SECOND / FRAMES_PER_SECOND;
  private static final double BALL_RADIUS = 10D;
  private static final double MAX_INITIAL_X_SPEED = 8D;
  private static final double MIN_INITIAL_X_SPEED = 0.5D;
  private static final double MAX_INITIAL_Y_SPEED = 12D;
  private static final double MIN_INITIAL_Y_SPEED = 7D;
  private static final boolean[] brickGrid = new boolean[ BRICKS_PER_ROW * BRICK_ROWS ];
  private HTMLCanvasElement _canvas;
  private CanvasRenderingContext2D _context;
  private double _ballX;
  private double _ballY;
  private double _ballSpeedX;
  private double _ballSpeedY;
  private boolean _simulationActive = true;
  private boolean _showMouseCoords = false;
  private boolean _showBrickCoords = false;
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

  private void resetBricks()
  {
    Arrays.fill( brickGrid, true );
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
      _showBrickCoords = false;
    }
    // the 2 key turns on debugging in brick coordinates
    else if ( "2".equals( event.key ) )
    {
      _showBrickCoords = !_showBrickCoords;
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

    ballBrickCollisionDetection();
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
      if ( _ballSpeedX < 0 )
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

    _ballX = _canvas.width / 2D;
    _ballY = ( BRICK_HEIGHT * BRICK_ROWS ) + SPACE_ABOVE_BRICKS + BRICK_HEIGHT;
  }

  private void ballBrickCollisionDetection()
  {
    final int ballBrickCol = toBrickColumn( _ballX );
    final int ballBrickRow = toBrickRow( _ballY );
    if ( isValidBrickCoordinates( ballBrickCol, ballBrickRow ) )
    {
      if ( brickGrid[ brickIndex( ballBrickCol, ballBrickRow ) ] )
      {
        brickGrid[ brickIndex( ballBrickCol, ballBrickRow ) ] = false;

        final int prevBallBrickCol = toBrickColumn( _ballX - _ballSpeedX );
        final int prevBallBrickRow = toBrickRow( _ballY - _ballSpeedY );
        if ( prevBallBrickCol != ballBrickCol )
        {
          if (
            // Don't reflect if we hit a horizontal surface
            !brickGrid[ brickIndex( prevBallBrickCol, ballBrickRow ) ] ||

            // This next condition handles the scenario where hit inside corner where we still want to reverse
            brickGrid[ brickIndex( ballBrickCol, prevBallBrickRow ) ] )
          {
            _ballSpeedX = -_ballSpeedX;
          }
        }
        if ( prevBallBrickRow != ballBrickRow )
        {
          if (
            // Don't reflect if we hit a vertical surface
            !brickGrid[ brickIndex( ballBrickCol, prevBallBrickRow ) ] ||

            // This next condition handles the scenario where hit inside corner where we still want to reverse
            brickGrid[ brickIndex( prevBallBrickCol, ballBrickRow ) ] )
          {
            _ballSpeedY = -_ballSpeedY;
          }
        }
      }
    }
  }

  private void resetGame()
  {
    resetBricks();
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

    drawBricks();

    // Ball
    drawCircle( _ballX, _ballY, BALL_RADIUS, "red" );

    if ( _showMouseCoords )
    {
      drawText( _mouseX, _mouseY, _mouseX + "," + _mouseY, "yellow" );
    }
    else if ( _showBrickCoords )
    {
      final double brickCol = toBrickColumn( _mouseX );
      final double brickRow = toBrickRow( _mouseY );
      if ( isValidBrickCoordinates( brickCol, brickRow ) )
      {
        drawText( _mouseX, _mouseY, Math.floor( brickCol ) + "," + Math.floor( brickRow ), "yellow" );
      }
    }
  }

  private boolean isValidBrickCoordinates( final double brickCol, final double brickRow )
  {
    return brickCol >= 0 && brickCol < BRICKS_PER_ROW && brickRow >= 0 && brickRow < BRICK_ROWS;
  }

  private int toBrickRow( final double mouseY )
  {
    return (int) Math.floor( ( mouseY - SPACE_ABOVE_BRICKS ) / BRICK_HEIGHT );
  }

  private int toBrickColumn( final double x )
  {
    return (int) Math.floor( x / BRICK_WIDTH );
  }

  private void drawBricks()
  {
    for ( int i = 0; i < BRICK_ROWS; i++ )
    {
      final double rowY = SPACE_ABOVE_BRICKS + i * BRICK_HEIGHT;
      for ( int j = 0; j < BRICKS_PER_ROW; j++ )
      {
        if ( brickGrid[ brickIndex( j, i ) ] )
        {
          drawRect( BRICK_WIDTH * j, rowY, BRICK_WIDTH - BRICK_GAP, BRICK_HEIGHT - BRICK_GAP, "blue" );
        }
      }
    }
  }

  private int brickIndex( final int column, final int row )
  {
    return row * BRICKS_PER_ROW + column;
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
