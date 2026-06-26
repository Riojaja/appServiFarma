import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Anular } from './anular';

describe('Anular', () => {
  let component: Anular;
  let fixture: ComponentFixture<Anular>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Anular]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Anular);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
