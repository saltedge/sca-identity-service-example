#\ -w -p 4567
require './app'

use Rack::Reloader
use DashboardController
use SCAController
run UserAuthorizeController
