import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Base64;
import java.util.Map;
import javax.crypto.SecretKey;

public class JwtDebug {
    public static void main(String[] args) {
        try {
            String secret = "mysecretkeymustbe32characterslongforhmacsha256!!";
            String base64Secret = Base64.getEncoder().encodeToString(secret.getBytes());
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());

            String token = Jwts.builder()
                    .claims(Map.of("userId", 123))
                    .signWith(key)
                    .compact();

            System.out.println("Token: " + token);

            // Simulate JwtService parsing
            byte[] decodedKey = Base64.getDecoder().decode(base64Secret);
            SecretKey parseKey = Keys.hmacShaKeyFor(decodedKey);

            Jws<Claims> jws = Jwts.parser()
                    .verifyWith(parseKey)
                    .build()
                    .parseSignedClaims(token);

            System.out.println("Parsed UserId: " + jws.getPayload().get("userId"));
            System.out.println("Success!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
