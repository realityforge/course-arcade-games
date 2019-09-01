package org.realityforge.arcade.adventure;

import com.google.gwt.core.client.EntryPoint;
import elemental2.dom.DOMRect;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLCanvasElement;
import elemental2.dom.HTMLHtmlElement;
import elemental2.dom.HTMLImageElement;
import elemental2.dom.KeyboardEvent;
import elemental2.dom.MouseEvent;
import javax.annotation.Nonnull;

public class Adventure
  implements EntryPoint
{
  private static final int FRAMES_PER_SECOND = 30;
  private static final int MILLIS_PER_SECOND = 1000;
  private static final int FRAME_DELAY = MILLIS_PER_SECOND / FRAMES_PER_SECOND;
  private final World _world = new World();
  private final Warrior _warrior = new Warrior( "Blue Storm" );
  private Renderer _renderer;
  private boolean _simulationActive = true;
  private boolean _showMouseCoords = false;
  private boolean _showTrackCoords = false;
  private boolean _car1ToMouse = false;
  private double _mouseX;
  private double _mouseY;
  private ImageAssets _imageAssets;
  private final HTMLImageElement[] _tiles = new HTMLImageElement[ World.MAX_CELL_TYPE_COUNT ];

  @Override
  public void onModuleLoad()
  {
    _renderer = new Renderer();

    // Render a loading screen for when the network is slow...
    final HTMLCanvasElement canvas = _renderer.getCanvas();
    _renderer.drawRect( 0, 0, canvas.width, canvas.height, "black" );
    _renderer.drawText( canvas.width / 2D, canvas.height / 2D, "Loading...", "white" );

    _imageAssets = new ImageAssets( this::onReady );
  }

  private void onReady()
  {
    _tiles[ 0 ] = _imageAssets.getImageByName( "world_road" );
    _tiles[ 1 ] = _imageAssets.getImageByName( "world_wall" );
    _tiles[ 2 ] = _imageAssets.getImageByName( "world_goal" );
    _tiles[ 3 ] = _imageAssets.getImageByName( "world_tree" );
    _tiles[ 4 ] = _imageAssets.getImageByName( "world_flag" );
    _tiles[ 5 ] = _tiles[ 0 ];
    _tiles[ 6 ] = _tiles[ 0 ];

    _renderer.getCanvas().addEventListener( "mousemove", e -> calculateMousePosition( (MouseEvent) e ) );
    DomGlobal.document.addEventListener( "keydown", e -> onKeyPress( (KeyboardEvent) e ) );
    DomGlobal.document.addEventListener( "keyup", e -> onKeyRelease( (KeyboardEvent) e ) );

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
    // the 3 key instantly transports car to mouse and changes direction to left direction
    else if ( "3".equals( event.key ) )
    {
      carToMouse( _warrior );
    }
    // the 4 key transports car to mouse when the mouse moves
    else if ( "5".equals( event.key ) )
    {
      _car1ToMouse = !_car1ToMouse;
    }
    else if ( !controlKey( event, true ) )
    {
      return;
    }
    event.preventDefault();
  }

  private void onKeyRelease( @Nonnull final KeyboardEvent event )
  {
    controlKey( event, false );
  }

  private boolean controlKey( @Nonnull final KeyboardEvent event, final boolean hold )
  {
    if ( "ArrowLeft".equals( event.code ) )
    {
      _warrior.setLeftHeld( hold );
    }
    else if ( "ArrowRight".equals( event.code ) )
    {
      _warrior.setRightHeld( hold );
    }
    else if ( "ArrowUp".equals( event.code ) )
    {
      _warrior.setUpHeld( hold );
    }
    else if ( "ArrowDown".equals( event.code ) )
    {
      _warrior.setDownHeld( hold );
    }
    else
    {
      return false;
    }
    event.preventDefault();
    return true;
  }

  @SuppressWarnings( { "unused" } )
  private void calculateMousePosition( @Nonnull final MouseEvent event )
  {
    final DOMRect rect = _renderer.getCanvas().getBoundingClientRect();
    final HTMLHtmlElement root = DomGlobal.document.documentElement;

    // The clientX/clientY properties are the coordinates relative to the client area of the mouse
    // pointer when a mouse event was triggered.. The client area is the current window.
    // Thus translating it according to component and scrolling will get coordinate within component.
    _mouseX = event.clientX - rect.x - root.scrollLeft;
    _mouseY = event.clientY - rect.top - root.scrollTop;

    if ( _car1ToMouse )
    {
      carToMouse( _warrior );
    }
  }

  private void carToMouse( @Nonnull final Warrior warrior )
  {
    final Body body = warrior.getBody();
    body.setX( _mouseX );
    body.setY( _mouseY );
    body.setAngle( 0 );
    body.setSpeed( 0 );
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
    _warrior.update();

    carCollisionDetection( _warrior );
  }

  private void resetCar( @Nonnull final Warrior warrior, final int startCellType )
  {
    final Body body = warrior.getBody();
    body.setSpeed( 0 );
    body.setAngle( Math.PI * 0.5D );

    final WorldPosition startCell = _world.getFirstCellMatching( startCellType );
    assert null != startCell;
    body.setX( startCell.getColumn() * World.CELL_WIDTH + ( World.CELL_WIDTH / 2 ) );
    body.setY( startCell.getRow() * World.CELL_HEIGHT + ( World.CELL_HEIGHT / 2 ) );
  }

  private void carCollisionDetection( @Nonnull final Warrior warrior )
  {
    final Body body = warrior.getBody();
    final int cell = _world.getCell( body );
    if ( World.CELL_GOAL_TYPE == cell )
    {
      DomGlobal.console.log( warrior.getName() + " wins!" );
      resetGame();
    }
    else if ( _world.isSolid( cell ) )
    {
      // This is to reverse action of frame to avoid car getting stuck in the wall before we reverse direction
      // otherwise next frame could see car try to reverse out when inside the wall and not make it out
      warrior.reverseMove();
    }
  }

  private void resetGame()
  {
    resetCar( _warrior, World.CELL_PLAYER1_START_TYPE );
  }

  private void renderWorld()
  {
    drawWorld();

    renderWarrior( _warrior, "warrior" );

    if ( _showMouseCoords )
    {
      _renderer.drawText( _mouseX, _mouseY, _mouseX + "," + _mouseY, "yellow" );
    }
    else if ( _showTrackCoords )
    {
      final double trackCol = _world.toCellColumn( _mouseX );
      final double trackRow = _world.toCellRow( _mouseY );
      if ( _world.isValidCell( trackCol, trackRow ) )
      {
        _renderer.drawText( _mouseX, _mouseY, Math.floor( trackCol ) + "," + Math.floor( trackRow ), "yellow" );
      }
    }
  }

  private void renderWarrior( @Nonnull final Warrior warrior, @Nonnull final String image )
  {
    final Body body = warrior.getBody();
    final HTMLImageElement carImage = _imageAssets.getImageByName( image );
    _renderer.drawImageWithRotation( carImage, body.getX(), body.getY(), body.getAngle() );
  }

  private void drawWorld()
  {
    int index = 0;
    double rowY = 0;
    for ( int i = 0; i < World.ROW_COUNT; i++ )
    {
      double cellX = 0;
      for ( int j = 0; j < World.COLUMN_COUNT; j++ )
      {
        _renderer.drawImage( _tiles[ _world.getCellAtIndex( index++ ) ], cellX, rowY );
        cellX += World.CELL_WIDTH;
      }
      rowY += World.CELL_HEIGHT;
    }
  }
}
