package org.realityforge.arcade.tennis;

import com.google.gwt.core.client.EntryPoint;
import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.DOMRect;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLCanvasElement;
import elemental2.dom.HTMLHtmlElement;
import elemental2.dom.MouseEvent;
import javax.annotation.Nonnull;
import jsinterop.base.Js;

public class Tennis
  implements EntryPoint
{
  private static final int WINNING_SCORE = 3;
  private static final int FRAMES_PER_SECOND = 60;
  private static final int MILLIS_PER_SECOND = 1000;
  private static final int FRAME_DELAY = MILLIS_PER_SECOND / FRAMES_PER_SECOND;
  private static final double BALL_RADIUS = 10D;
  private static final double PADDLE_HEIGHT = 100D;
  private static final double HALF_PADDLE_HEIGHT = PADDLE_HEIGHT / 2;
  private static final double PADDLE_WIDTH = 10D;
  // The zone inside panel that the computer player targets
  private static final double AI_PADDLE_TARGET_ZONE = PADDLE_HEIGHT * 0.7D;
  //
  private static final double MAX_REFLECT_SPEED = 12.5D;
  private static final double VERTICAL_REFLECT_FORCE_TRANSFER = ( 2 * MAX_REFLECT_SPEED ) / PADDLE_HEIGHT;
  private static final double INITIAL_X_SPEED = 5D;
  private static final double INITIAL_Y_SPEED = 3D;
  private HTMLCanvasElement _canvas;
  private CanvasRenderingContext2D _context;
  private double _ballX = 50D;
  private double _ballY = 50D;
  private double _ballSpeedX = INITIAL_X_SPEED;
  private double _ballSpeedY = INITIAL_Y_SPEED;
  private double _paddle1Y = 200D;
  private double _paddle2Y = 200D;
  private int _player1Score;
  private int _player2Score;
  private boolean _showingWinScreen;

  @Override
  public void onModuleLoad()
  {
    _canvas = (HTMLCanvasElement) DomGlobal.document.getElementById( "gameCanvas" );
    _context = Js.uncheckedCast( _canvas.getContext( "2d" ) );

    _canvas.addEventListener( "mousedown", e -> onMouseClick() );
    _canvas.addEventListener( "mousemove", e -> calculateMousePosition( (MouseEvent) e ) );

    runFrame();
    DomGlobal.setInterval( v -> runFrame(), FRAME_DELAY );
  }

  private void onMouseClick()
  {
    if ( _showingWinScreen )
    {
      _player1Score = 0;
      _player2Score = 0;
      _showingWinScreen = false;
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
    final double mouseX = event.clientX - rect.x - root.scrollLeft;
    final double mouseY = event.clientY - rect.top - root.scrollTop;

    // Our pointer should be center of paddle and the paddle can not go outside bounds
    _paddle1Y = limitPaddleToScreen( mouseY - HALF_PADDLE_HEIGHT );
  }

  // Make sure paddle never goes off screen
  private double limitPaddleToScreen( final double paddlePosition )
  {
    return Math.min( Math.max( 0, paddlePosition ), _canvas.height - PADDLE_HEIGHT );
  }

  private void runFrame()
  {
    if ( _showingWinScreen )
    {
      renderWinScreen();
    }
    else
    {
      simulateWorld();
      renderWorld();
    }
  }

  private void simulateWorld()
  {
    computerPlayer();
    _ballX += _ballSpeedX;
    _ballY += _ballSpeedY;
    if ( _ballX - BALL_RADIUS < 0 )
    {
      if ( ( _ballY + BALL_RADIUS < _paddle1Y ) || ( _ballY - BALL_RADIUS > _paddle1Y + PADDLE_HEIGHT ) )
      {
        // Score changed before ballReset as winning condition check occurs inside ballReset
        _player2Score++;
        ballReset();
      }
      else
      {
        _ballSpeedX = -_ballSpeedX;

        // Gives some ball control so that depending on where you hit the ball wil depend on what angle
        // and speed the ball returns to other side.
        _ballSpeedY = ( _ballY - ( _paddle1Y + HALF_PADDLE_HEIGHT ) ) * VERTICAL_REFLECT_FORCE_TRANSFER;
      }
    }
    if ( ( _ballX + BALL_RADIUS ) > _canvas.width )
    {
      if ( ( _ballY + BALL_RADIUS < _paddle2Y ) || ( _ballY - BALL_RADIUS > _paddle2Y + PADDLE_HEIGHT ) )
      {
        // Score changed before ballReset as winning condition check occurs inside ballReset
        _player1Score++;
        ballReset();
      }
      else
      {
        _ballSpeedX = -_ballSpeedX;

        // Ball control for player 2
        _ballSpeedY = ( _ballY - ( _paddle2Y + HALF_PADDLE_HEIGHT ) ) * VERTICAL_REFLECT_FORCE_TRANSFER;
      }
    }

    if ( ( _ballY + BALL_RADIUS ) > _canvas.height || _ballY < BALL_RADIUS )
    {
      _ballSpeedY = -_ballSpeedY;
    }
  }

  private void computerPlayer()
  {
    // The computer player aims to get the ball in the center of it's target
    // zone but once the ball is in the target zone it will not move the paddle.
    // This creates more realistic movement and stops paddle jitter as step size
    // is constant  and otherwise the AI would constantly be adjusting
    final double paddle2Center = _paddle2Y + HALF_PADDLE_HEIGHT;

    if ( paddle2Center < _ballY - ( AI_PADDLE_TARGET_ZONE / 2D ) )
    {
      _paddle2Y = limitPaddleToScreen( _paddle2Y + 6 );
    }
    else if ( paddle2Center > _ballY + ( AI_PADDLE_TARGET_ZONE / 2D ) )
    {
      _paddle2Y = limitPaddleToScreen( _paddle2Y - 6 );
    }
  }

  private void ballReset()
  {
    if ( _player1Score >= WINNING_SCORE || _player2Score >= WINNING_SCORE )
    {
      _showingWinScreen = true;
    }
    _ballSpeedX = -Math.min( _ballSpeedX, INITIAL_X_SPEED );
    _ballSpeedY = Math.min( _ballSpeedY / _ballSpeedX, 2 ) * -INITIAL_Y_SPEED;
    _ballX = _canvas.width / 2D;
    _ballY = _canvas.height / 2D;
  }

  private void renderWinScreen()
  {
    clearBackground();

    _context.fillStyle = CanvasRenderingContext2D.FillStyleUnionType.of( "white" );
    if ( _player1Score >= WINNING_SCORE )
    {
      _context.fillText( "Player 1 is the winner! Huzzah!", 120D, 300D );
    }
    else
    {
      _context.fillText( "Player 2 is the winner! Huzzah!", 500D, 300D );
    }
    _context.fillText( "Click to continue", 350D, 500D );
  }

  private void renderWorld()
  {
    // Background
    clearBackground();

    // Player Paddle
    drawRect( 0D, _paddle1Y, PADDLE_WIDTH, PADDLE_HEIGHT, "white" );

    // Computer Paddle
    drawRect( _canvas.width - PADDLE_WIDTH, _paddle2Y, PADDLE_WIDTH, PADDLE_HEIGHT, "white" );

    // Ball
    drawCircle( _ballX, _ballY, BALL_RADIUS, "red" );

    // Draw net
    for ( int i = 0; i < _canvas.height; i += 40 )
    {
      drawRect( _canvas.width / 2D - 1D, i, 2, 20, "white" );
    }

    // Draw scores
    _context.fillStyle = CanvasRenderingContext2D.FillStyleUnionType.of( "white" );
    _context.fillText( "P1 Score: " + _player1Score, 100D, 100D );
    // Assume 120 pixels to represent text
    _context.fillText( "P2 Score: " + _player2Score, _canvas.width - 100D - 120D, 100D );
  }

  private void clearBackground()
  {
    drawRect( 0D, 0D, _canvas.width, _canvas.height, "black" );
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
  private void drawRect( final double x,
                         final double y,
                         final double width,
                         final double height,
                         @Nonnull final String color )
  {
    _context.fillStyle = CanvasRenderingContext2D.FillStyleUnionType.of( color );
    _context.fillRect( x, y, width, height );
  }
}
