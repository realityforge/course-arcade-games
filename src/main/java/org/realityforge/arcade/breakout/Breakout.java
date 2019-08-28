package org.realityforge.arcade.breakout;

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

public class Breakout
  implements EntryPoint
{
  private static final int FRAMES_PER_SECOND = 30;
  private static final int MILLIS_PER_SECOND = 1000;
  private static final int FRAME_DELAY = MILLIS_PER_SECOND / FRAMES_PER_SECOND;
  private static final double BALL_RADIUS = 10D;
  private static final double PADDLE_HEIGHT = 10D;
  private static final double PADDLE_WIDTH = 100D;
  private static final double HALF_PADDLE_WIDTH = PADDLE_WIDTH / 2;
  private static final double MAX_INITIAL_X_SPEED = 8D;
  private static final double MIN_INITIAL_X_SPEED = 0.5D;
  private static final double MAX_INITIAL_Y_SPEED = 8D;
  private static final double MIN_INITIAL_Y_SPEED = 4D;
  private static final double MAX_REFLECT_SPEED = 12.5D;
  // This is multiplied by the distance from the center of the paddle. So maximum force is transfered when you
  // ht the edges of the paddle while hitting the center of the paddle results in almost vertical reflection
  private static final double HORIZONTAL_REFLECT_FORCE_TRANSFER = ( 2 * MAX_REFLECT_SPEED ) / PADDLE_WIDTH;
  // The amount that the paddle is inset from the bottom of the screen
  private static final double PADDLE_Y_INSET = 50D;
  private static final double BRICK_WIDTH = 100D;
  private static final double BRICK_HEIGHT = 40D;
  private static final double BRICK_GAP = 2D;
  private static final double SPACE_ABOVE_BRICKS = BRICK_HEIGHT;
  private static final int BRICKS_PER_ROW = 8;
  private static final int BRICK_ROWS = 2;
  private static final boolean[] brickGrid = new boolean[ BRICKS_PER_ROW * BRICK_ROWS ];
  private HTMLCanvasElement _canvas;
  private CanvasRenderingContext2D _context;
  private double _ballX;
  private double _ballY;
  private double _ballSpeedX;
  private double _ballSpeedY;
  private double _paddlePositionX;
  private boolean _simulationActive = true;
  private boolean _showMouseCoords = false;
  private boolean _showBrickCoords = false;
  private double _mouseX;
  private double _mouseY;

  @Override
  public void onModuleLoad()
  {
    _canvas = (HTMLCanvasElement) DomGlobal.document.getElementById( "gameCanvas" );
    _context = Js.uncheckedCast( _canvas.getContext( "2d" ) );

    // Center paddle
    _paddlePositionX = _canvas.width / 2D - ( PADDLE_WIDTH / 2D );
    ballReset();

    _canvas.addEventListener( "mousemove", e -> calculateMousePosition( (MouseEvent) e ) );
    DomGlobal.document.addEventListener( "keydown", e -> onKeyPress( (KeyboardEvent) e ) );

    resetBricks();

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

    // Our pointer should be center of paddle and the paddle can not go outside bounds
    _paddlePositionX = limitPaddleToScreen( _mouseX - HALF_PADDLE_WIDTH );
  }

  // Make sure paddle never goes off screen
  private double limitPaddleToScreen( final double paddlePosition )
  {
    return Math.min( Math.max( 0, paddlePosition ), _canvas.width - PADDLE_WIDTH );
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
    _ballX += _ballSpeedX;
    _ballY += _ballSpeedY;

    final double ballTopY = _ballY - BALL_RADIUS;
    final double ballBottomY = _ballY + BALL_RADIUS;
    final double ballLeftX = _ballX - BALL_RADIUS;
    final double ballRightX = _ballX + BALL_RADIUS;

    // Bounce off the top edge
    if ( ballTopY < 0 )
    {
      _ballSpeedY = -_ballSpeedY;
    }
    // Reset ball if off the bottom edge
    else if ( ballBottomY > _canvas.height )
    {
      ballReset();
    }
    // Bounce off the side edges
    else if ( ballRightX > _canvas.width || ballLeftX < 0 )
    {
      _ballSpeedX = -_ballSpeedX;
    }

    final double paddleTopY = _canvas.height - PADDLE_Y_INSET;
    final double paddleBottomY = paddleTopY + PADDLE_HEIGHT;
    final double paddleLeftX = _paddlePositionX;
    final double paddleRightX = _paddlePositionX + PADDLE_WIDTH;

    if ( ballRightX > paddleLeftX &&
         ballLeftX < paddleRightX &&
         ballTopY < paddleBottomY &&
         ballBottomY > paddleTopY )
    {
      _ballSpeedY = -_ballSpeedY;

      // This gives ball control as in Tennis game
      final double paddleCenter = _paddlePositionX + HALF_PADDLE_WIDTH;
      final double ballDistanceFromPaddleCenter = _ballX - paddleCenter;
      _ballSpeedX = ballDistanceFromPaddleCenter * HORIZONTAL_REFLECT_FORCE_TRANSFER;
    }
  }

  private void ballReset()
  {
    _ballSpeedX = ( Math.random() < 0.5 ? -1D : 1D ) * randomValue( MIN_INITIAL_X_SPEED, MAX_INITIAL_X_SPEED );
    _ballSpeedY = randomValue( MIN_INITIAL_Y_SPEED, MAX_INITIAL_Y_SPEED );

    _ballX = _canvas.width / 2D;
    _ballY = _canvas.height / 2D;
  }

  private double randomValue( final double min, final double max )
  {
    return ( Math.random() * ( max - min ) ) + min;
  }

  private void renderWorld()
  {
    // Background
    clearBackground();

    // Player Paddle
    drawRect( _paddlePositionX, _canvas.height - PADDLE_Y_INSET, PADDLE_WIDTH, PADDLE_HEIGHT, "white" );

    drawBricks();

    // Ball
    drawCircle( _ballX, _ballY, BALL_RADIUS, "red" );

    if ( _showMouseCoords )
    {
      drawText( _mouseX, _mouseY, _mouseX + "," + _mouseY, "yellow" );
    }
    else if ( _showBrickCoords )
    {
      final double brickCol = _mouseX / BRICK_WIDTH;
      final double brickRow = ( _mouseY - SPACE_ABOVE_BRICKS ) / BRICK_HEIGHT;
      if ( brickRow > 0 && brickRow < BRICK_ROWS )
      {
        drawText( _mouseX, _mouseY, Math.floor( brickCol ) + "," + Math.floor( brickRow ), "yellow" );
      }
    }
  }

  private void drawBricks()
  {
    for ( int i = 0; i < BRICK_ROWS; i++ )
    {
      final double rowY = SPACE_ABOVE_BRICKS + i * BRICK_HEIGHT;
      for ( int j = 0; j < BRICKS_PER_ROW; j++ )
      {
        if ( brickGrid[ i * BRICKS_PER_ROW + j ] )
        {
          drawRect( BRICK_WIDTH * j, rowY, BRICK_WIDTH - BRICK_GAP, BRICK_HEIGHT - BRICK_GAP, "blue" );
        }
      }
    }
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
