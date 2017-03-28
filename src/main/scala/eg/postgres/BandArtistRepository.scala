package eg
package postgres

import org.atnos.eff._
// import org.atnos.eff.addon.fs2._

final class BandArtistRepository[S] extends eg.BandArtistRepository[Eff[S, ?]] {

  def create(artist: Artist, band: Band): Eff[S, (ArtistId, BandId)] = Eff.pure((ArtistId(0), BandId(0)))
  def createAritst(artist: Artist): Eff[S, ArtistId] = Eff.pure(ArtistId(0))
  def createBand(band: Band): Eff[S, BandId] = Eff.pure(BandId(0))

  def linkArtistToBand(artist: ArtistId, band: BandId): Eff[S, Unit] = Eff.pure(())

  def findBandsWithArtists(): Eff[S, Vector[BandArtistView]] = Eff.pure(Vector())
}
