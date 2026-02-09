package entity.services;

import java.awt.Color;
import java.awt.Point;

import entity.Entity;
import entity.Particle;

public class ParticleService {

  private final Entity owner;

  public ParticleService(Entity owner) {
    this.owner = owner;
  }

  public void generateParticle(Entity generator, Entity target) {
    Color color = generator.getParticleColor();
    int size = generator.getParticleSize();
    int speedVal = generator.getParticleSpeed();
    int maxLifeVal = generator.getParticleMaxLife();

    Particle p1 = new Particle(owner.getGp(), target, color, size, speedVal, maxLifeVal, new Point(-2, -1));
    Particle p2 = new Particle(owner.getGp(), target, color, size, speedVal, maxLifeVal, new Point(2, -1));
    Particle p3 = new Particle(owner.getGp(), target, color, size, speedVal, maxLifeVal, new Point(-2, 1));
    Particle p4 = new Particle(owner.getGp(), target, color, size, speedVal, maxLifeVal, new Point(2, 1));

    var particles = owner.getGp().getParticleList();
    particles.add(p1);
    particles.add(p2);
    particles.add(p3);
    particles.add(p4);
  }
}
