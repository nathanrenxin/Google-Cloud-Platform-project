/*
 * Copyright (C) 2009 Swedish Institute of Computer Science (SICS) Copyright (C)
 * 2009 Royal Institute of Technology (KTH)
 *
 * KompicsToolbox is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package se.kth.ledbat.util;

/**
 * exact implementation of https://tools.ietf.org/html/rfc6298
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class RTTEstimator {
  private final LedbatConfig config;
  
  private long rto = -1;
  private long srtt;
  private long rttvar;
  
  public RTTEstimator(LedbatConfig config) {
    this.config = config;
  }
  
  
  public void update(long r) {
    if(rto == -1) {
      updateFirst(r);
    } else {
      updateNext(r);
    }
    rto = srtt + Math.max(config.G, config.K*rttvar);
    rto = Math.max(config.MAX_RTO, Math.min(config.MIN_RTO, rto));
  }
  
  private void updateFirst(long r) {
    srtt = r;
    rttvar = r/2;
  }
  
  private void updateNext(long r) {
    rttvar = (long)((1 - config.BETA) * rttvar + (config.BETA * Math.abs(srtt - r)));
    srtt = (long)((1 - config.ALPHA) * srtt + config.ALPHA * r);
  }
  
  public long rto() {
    if(rto == -1) {
      return config.MIN_RTO;
    } else {
      return rto;
    }
  }
}
