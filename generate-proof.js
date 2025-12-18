import fs from "fs";
import crypto from "crypto";

// 1. Read commit hash
const commitHash = fs.readFileSync("commit.txt", "utf8").trim();

// 2. Load keys
const studentPrivateKey = fs.readFileSync("student_private.pem", "utf8");
const instructorPublicKey = fs.readFileSync("instructor_public.pem", "utf8");

// 3. Sign commit hash using RSA-PSS SHA-256
const signature = crypto.sign("sha256", Buffer.from(commitHash), {
  key: studentPrivateKey,
  padding: crypto.constants.RSA_PKCS1_PSS_PADDING,
//  saltLength: crypto.constants.RSA_PSS_SALTLEN_MAX_SIGN
  saltLength: 32
});

// 4. Encrypt signature using RSA-OAEP SHA-256 + MGF1 SHA-256
const encrypted = crypto.publicEncrypt(
  {
    key: instructorPublicKey,
    padding: crypto.constants.RSA_PKCS1_OAEP_PADDING,
    oaepHash: "sha256"
  },
  signature
);

// 5. Base64 encode
const proofBase64 = encrypted.toString("base64");

// 6. Save output
fs.writeFileSync("proof.txt", proofBase64);

console.log("\n===== GIT COMMIT PROOF GENERATED =====\n");
console.log(proofBase64);
console.log("\nSaved to proof.txt\n");
