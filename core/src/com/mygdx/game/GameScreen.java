package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;
import java.util.Iterator;
import java.util.stream.IntStream;

public class GameScreen extends ScreenAdapter {

  public static final int SCREEN_WIDTH = 800;
  public static final int SCREEN_HEIGHT = 480;

  private final GdxGame1 game;
  private final OrthographicCamera camera;
  private final Array<Disposable> disposables;

  private final Texture heartTexture;
  private final Texture rainDropTexture;
  private final Texture bucketTexture;
  private final Texture backgroundTexture;

  private final Rectangle bucket;

  private final Music rainMusic;
  private final Sound dropSound;

  private Array<Rectangle> rainDrops;
  private long lastDropTime;
  private int dropSpeed;
  private int score;
  private int lives;

  public GameScreen(GdxGame1 game) {
    this.game = game;
    this.disposables = new Array<>();
    resetGame();

    camera = new OrthographicCamera();
    camera.setToOrtho(false, SCREEN_WIDTH, SCREEN_HEIGHT);

    backgroundTexture = toBeDisposed(new Texture("background.jpg"));
    heartTexture = toBeDisposed(new Texture("heart.png"));
    rainDropTexture = toBeDisposed(new Texture("drop.png"));
    bucketTexture = toBeDisposed(new Texture("bucket.png"));

    bucket = new Rectangle();
    bucket.x = SCREEN_WIDTH / 2 - bucketTexture.getWidth() / 2;
    bucket.y = 20;
    bucket.width = rainDropTexture.getWidth();
    bucket.height = rainDropTexture.getHeight();

    rainMusic = toBeDisposed(Gdx.audio.newMusic(Gdx.files.internal("rain.mp3")));
    dropSound = toBeDisposed(Gdx.audio.newSound(Gdx.files.internal("drop.wav")));

    rainMusic.setLooping(true);
  }

  @Override
  public void show() {
    rainMusic.play();
  }

  @Override
  public void render(float delta) {
    if (lives > 0) {
      update();
    }

    ScreenUtils.clear(1, 0, 0, 1);
    camera.update();

    game.batch.setProjectionMatrix(camera.combined);
    game.batch.begin();

    game.batch.draw(backgroundTexture, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

    IntStream.range(1, lives + 1).forEach(live -> {
      game.batch.draw(heartTexture, SCREEN_WIDTH - (heartTexture.getWidth() + 5) * live,
          SCREEN_HEIGHT - heartTexture.getHeight() - 10);
    });

    game.batch.draw(bucketTexture, bucket.x, bucket.y);
    game.font.draw(game.batch, "Score: " + score, 10, SCREEN_HEIGHT - 10);

    if (lives == 0) {
      game.font.draw(game.batch, "Press ENTER to restart", 270, 240);
      game.font.draw(game.batch, "Press ESC to exit", 270, 200);

      if (Gdx.input.isKeyPressed(Keys.ENTER)) {
        resetGame();
      }

      if (Gdx.input.isKeyPressed(Keys.ESCAPE)) {
        Gdx.app.exit();
      }
    }

    rainDrops.forEach(drop -> game.batch.draw(rainDropTexture, drop.x, drop.y));
    game.batch.end();
  }

  @Override
  public void dispose() {
    disposables.forEach(Disposable::dispose);
  }

  private void update() {
    if (TimeUtils.nanoTime() - lastDropTime > 1000000000) {
      spawnRainDrop();
    }

    for (Iterator<Rectangle> iter = rainDrops.iterator(); iter.hasNext(); ) {
      Rectangle raindrop = iter.next();
      raindrop.y -= dropSpeed * Gdx.graphics.getDeltaTime();
      if (raindrop.y + rainDropTexture.getHeight() < 0) {
        lives--;
        iter.remove();
      }
      if (raindrop.overlaps(bucket)) {
        dropSound.play();
        score++;
        dropSpeed += 10;
        iter.remove();
      }
    }

    if (Gdx.input.isTouched()) {
      Vector3 touchPos = new Vector3();
      touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
      camera.unproject(touchPos);
      bucket.setX(touchPos.x - bucketTexture.getWidth() / 2);
    }

    if (Gdx.input.isKeyPressed(Keys.LEFT)) {
      bucket.setX(bucket.getX() - 200 * Gdx.graphics.getDeltaTime());
    }

    if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
      bucket.setX(bucket.getX() + 200 * Gdx.graphics.getDeltaTime());
    }

    if (bucket.x < 0) {
      bucket.x = 0;
    }
    if (bucket.x > SCREEN_WIDTH - 64) {
      bucket.x = SCREEN_WIDTH - 64;
    }
  }

  private void resetGame() {
    score = 0;
    lives = 3;
    dropSpeed = 200;
    rainDrops = new Array<>();
  }

  private void spawnRainDrop() {
    Rectangle raindrop = new Rectangle();
    raindrop.x = MathUtils.random(0, SCREEN_WIDTH - rainDropTexture.getWidth());
    raindrop.y = SCREEN_HEIGHT;
    raindrop.width = rainDropTexture.getWidth();
    raindrop.height = rainDropTexture.getHeight();
    rainDrops.add(raindrop);
    lastDropTime = TimeUtils.nanoTime();
  }

  private <T extends Disposable> T toBeDisposed(T object) {
    return object;
  }
}
