using UnityEngine;
using UnityEngine.XR;

namespace VRProject 
{
    public class VRController : MonoBehaviour
    {
        public Transform handTransform;
        private Vector3 velocity;
        
        void Start() 
        {
            XRSettings.enabled = true;
        }
        
        void Update() 
        {
            Vector3 position = handTransform.position;
            velocity = CalculateVelocity(position);
        }
        
        private Vector3 CalculateVelocity(Vector3 currentPos) 
        {
            return currentPos * Time.deltaTime;
        }
        
        public void OnTriggerPressed() 
        {
            Debug.Log("VR Trigger pressed");
        }
    }
}
