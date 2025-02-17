package com.shayarify.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.shayarify.dto.CommentDTO;
import com.shayarify.dto.PostDTO;
import com.shayarify.dto.UserDTO;
import com.shayarify.model.Post;
import com.shayarify.model.User;
import com.shayarify.repository.PostRepository;
import com.shayarify.repository.UserRepository;

@Service
public class PostServiceImplementation implements PostService {

	@Autowired
	PostRepository postRepository;

	@Autowired
	UserService userService;
	
	@Autowired
	UserRepository userRepository;

	@Override
	public Post createNewPost(Post post, Integer userId) throws Exception {

		User user = userService.findUserById(userId);

		Post newPost = new Post();
		newPost.setCaption(post.getCaption());
		newPost.setImage(post.getImage());
		newPost.setCreatedAt(LocalDateTime.now());
		newPost.setVideo(post.getVideo());
		newPost.setUser(user);
		
		return postRepository.save(newPost );
	}

	@Override
	public String deletePost(Integer postId, Integer userId) throws Exception {
		Post post = findPostById(postId);
		User user = userService.findUserById(userId);
		
		if(post.getUser().getId()!=user.getId()) {
			throw new Exception ("you can't delete another user post");
		}
		
		postRepository.delete(post);
		return "post deleted successfully";
	}

	@Override
	public List<Post> findPostByUserId(Integer userId) {

		return postRepository.findPostByUserId(userId);
	}

	@Override
	public Post findPostById(Integer postId) throws Exception {

		Optional<Post> opt = postRepository.findById(postId);
		
		if(opt.isEmpty()) {
			throw new Exception("post not found with id "+postId);
		}
		
		return opt.get();
	}

	@Override
	public List<PostDTO> findAllPost() {  // Ensure this returns List<PostDTO>
	    // Retrieve all posts from the repository
	    List<Post> posts = postRepository.findAll();

	    // Map each Post entity to a PostDTO
	    List<PostDTO> postDTOs = posts.stream()
	                                  .map(post -> {
	                                      // Map the User entity to UserDTO
	                                      UserDTO userDTO = new UserDTO(post.getUser().getId(), post.getUser().getFirstName(), 
	                                                                    post.getUser().getLastName(), post.getUser().getEmail(), 
	                                                                    post.getUser().isTermsAccepted(), post.getUser().getFollowers(), 
	                                                                    post.getUser().getFollowings(), post.getUser().getGender(),
	                                                                    post.getUser().getAvatar());
	                                      
	                                      // Map the liked users to UserDTOs
	                                      List<UserDTO> likedDTOs = post.getLiked().stream().map(user -> 
	                                              new UserDTO(user.getId(), user.getFirstName(), user.getLastName(), 
	                                                          user.getEmail(), user.isTermsAccepted(), 
	                                                          user.getFollowers(), user.getFollowings(), user.getGender(),user.getAvatar()))
	                                                                      .collect(Collectors.toList());
	                                      
	                                      // Map comments to CommentDTOs
	                                      List<CommentDTO> commentDTOs = post.getComments().stream()
	                                             .map(comment -> new CommentDTO(comment.getId(), comment.getContent(), 
	                                                                             new UserDTO(comment.getUser().getId(), 
	                                                                                         comment.getUser().getFirstName(),
	                                                                                         comment.getUser().getLastName(),
	                                                                                         comment.getUser().getEmail(),
	                                                                                         comment.getUser().isTermsAccepted(),
	                                                                                         comment.getUser().getFollowers(),
	                                                                                         comment.getUser().getFollowings(),
	                                                                                         comment.getUser().getGender(),
	                                                                                         comment.getUser().getAvatar())))
	                                             .collect(Collectors.toList());
	                                      
	                                      // Return a new PostDTO
	                                      return new PostDTO(post.getId(), post.getCaption(), post.getImage(), post.getVideo(), 
	                                                        userDTO, likedDTOs, post.getCreatedAt(), commentDTOs);
	                                  }).collect(Collectors.toList());

	    return postDTOs;
	}


	@Override
	public Post savedPost(Integer postId, Integer userId) throws Exception {
		Post post = findPostById(postId);
		User user = userService.findUserById(userId);
		
		if(user.getSavedPost().contains(post)) {
			user.getSavedPost().remove(post);
			
		}
		else user.getSavedPost().add(post);
		userRepository.save(user);
		return post;
	}

	@Override
	public Post likePost(Integer postId, Integer userId) throws Exception {
		Post post = findPostById(postId);
		User user = userService.findUserById(userId);
		
		if(post.getLiked().contains(user)) {
			post.getLiked().remove(user);
		}
		else {
			post.getLiked().add(user);
		}
		
		
		return postRepository.save(post);
	}

}
