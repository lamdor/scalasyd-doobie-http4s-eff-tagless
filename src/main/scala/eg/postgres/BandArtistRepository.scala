package eg
package postgres

import org.atnos.eff._
// import org.atnos.eff.addon.fs2._

final class BandArtistRepository[S] extends eg.BandArtistRepository[Eff[S, ?]] {
  def create(artist: Artist, band: Band): Eff[S, (ArtistId, BandId)] = ???
  def createAritst(artist: Artist): Eff[S, ArtistId] = ???
  def createBand(band: Band): Eff[S, BandId] = ???

  def linkArtistToBand(artist: ArtistId, band: BandId): Eff[S, Unit] = ???

  def findBandsWithArtists(): Eff[S, Vector[BandArtistView]] = ???
}
